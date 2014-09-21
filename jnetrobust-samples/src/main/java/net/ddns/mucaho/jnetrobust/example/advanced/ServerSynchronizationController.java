package net.ddns.mucaho.jnetrobust.example.advanced;


import net.ddns.mucaho.jnetrobust.example.DefaultHost;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.Vector2D;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.MODE;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.HOST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

public class ServerSynchronizationController implements Runnable {
    private final Vector2D data;
    private final HOST hostMode = HOST.SERVER;

    private SynchronizationGUI gui;
    private DefaultHost<Vector2D> hostA;
    private DefaultHost<Vector2D> hostB;
    private MODE updateModeA;
    private MODE updateModeB;

    public ServerSynchronizationController(final MODE updateModeA,
                                           final MODE updateModeB,
                                           InetSocketAddress hostAddressA,
                                           InetSocketAddress hostAddressB,
                                           InetSocketAddress clientAddressA,
                                           InetSocketAddress clientAddressB) throws IOException {

        this.updateModeA = updateModeA;
        this.updateModeB = updateModeB;
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new SynchronizationGUI(hostMode);
        gui.setVisible(true);


        hostA = new DefaultHost<Vector2D>(hostMode.toString(), hostAddressA, clientAddressA,
                Vector2D.class, new DefaultHost.DataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateModeA == MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final Vector2D newestData) {
                if (updateModeA == MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });

        hostB = new DefaultHost<Vector2D>(hostMode.toString(), hostAddressB, clientAddressB,
                Vector2D.class, new DefaultHost.DataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateModeB == MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final Vector2D newestData) {
                if (updateModeB == MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });

    }

    @Override
    public void run() {
        try {
            // receive from A
            Queue<Vector2D> receivedQueueA = hostA.receive();
            for (final Vector2D receivedData : receivedQueueA)
                if (updateModeA == MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // receive from B
            Queue<Vector2D> receivedQueueB = hostB.receive();
            for (final Vector2D receivedData : receivedQueueB)
                if (updateModeB == MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // route from A to B
            for (final Vector2D receivedData : receivedQueueA)
                 hostB.send(receivedData);

            // route from B to A
            for (final Vector2D receivedData : receivedQueueB)
                hostA.send(receivedData);

            // send server to both
            gui.sendGUI(data);
            hostA.send(data);
            hostB.send(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
