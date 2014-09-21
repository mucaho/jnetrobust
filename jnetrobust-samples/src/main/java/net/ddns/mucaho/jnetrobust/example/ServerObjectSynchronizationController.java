package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.example.ObjectSynchronizationController.Vector2D;

public class ServerObjectSynchronizationController implements Runnable {
    private final Vector2D data;
    private final ObjectSynchronization.HOST hostMode = ObjectSynchronization.HOST.SERVER;

    private ObjectSynchronizationGUI gui;
    private DefaultHost<ObjectSynchronizationController.Vector2D> hostA;
    private DefaultHost<ObjectSynchronizationController.Vector2D> hostB;
    private ObjectSynchronization.MODE updateModeA;
    private ObjectSynchronization.MODE updateModeB;

    public ServerObjectSynchronizationController(final ObjectSynchronization.MODE updateModeA,
                                                 final ObjectSynchronization.MODE updateModeB,
                                                 InetSocketAddress hostAddressA,
                                                 InetSocketAddress hostAddressB,
                                                 InetSocketAddress clientAddressA,
                                                 InetSocketAddress clientAddressB)
            throws IOException {

        this.updateModeA = updateModeA;
        this.updateModeB = updateModeB;
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new ObjectSynchronizationGUI(hostMode);
        gui.setVisible(true);

        Kryo serializer = new Kryo();
        serializer.register(Vector2D.class);
        serializer.register(MultiKeyValue.class);
        serializer.register(Packet.class);

        hostA = new DefaultHost<Vector2D>(hostMode.toString(), serializer,
                hostAddressA, clientAddressA, new DefaultHost.OrderedDataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateModeA == ObjectSynchronization.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final Vector2D newestData) {
                if (updateModeA == ObjectSynchronization.MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });

        hostB = new DefaultHost<Vector2D>(hostMode.toString(), serializer,
                hostAddressB, clientAddressB, new DefaultHost.OrderedDataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateModeB == ObjectSynchronization.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final Vector2D newestData) {
                if (updateModeB == ObjectSynchronization.MODE.UPDATE_ON_NEWEST_DATA)
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
                if (updateModeA == ObjectSynchronization.MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // receive from B
            Queue<Vector2D> receivedQueueB = hostB.receive();
            for (final Vector2D receivedData : receivedQueueB)
                if (updateModeB == ObjectSynchronization.MODE.UPDATE_ON_RECEIVED_DATA)
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
