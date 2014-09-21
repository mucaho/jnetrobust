package net.ddns.mucaho.jnetrobust.example.advanced;


import net.ddns.mucaho.jnetrobust.example.DefaultHost;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.Vector2D;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.MODE;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.HOST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;


public class ClientSynchronizationController implements Runnable {

    private SynchronizationGUI gui;
    private DefaultHost<Vector2D> host;
    private final MODE updateMode;
    private final Vector2D data;

    public ClientSynchronizationController(final HOST hostMode, final MODE updateMode,
                                           InetSocketAddress hostAddress,
                                           InetSocketAddress receiverAddress) throws IOException {

        this.updateMode = updateMode;
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new SynchronizationGUI(hostMode);
        gui.setVisible(true);


        host = new DefaultHost<Vector2D>(hostMode.toString(), hostAddress, receiverAddress,
                Vector2D.class, new DefaultHost.DataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateMode == MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final Vector2D newestData) {
                if (updateMode == MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });
    }



    @Override
    public void run() {
        try {
            // receive
            Queue<Vector2D> receivedQueue = host.receive();
            for (final Vector2D receivedData : receivedQueue)
                if (updateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // send
            gui.sendGUI(data);
            host.send(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}