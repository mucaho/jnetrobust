/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


import com.github.mucaho.jnetrobust.example.DefaultHost;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

public class ServerSynchronizationController implements Runnable {
    private final SynchronizationMain.Vector2D data;
    private final SynchronizationMain.HOST hostMode = SynchronizationMain.HOST.SERVER;

    private SynchronizationGUI gui;
    private DefaultHost<SynchronizationMain.Vector2D> hostA;
    private DefaultHost<SynchronizationMain.Vector2D> hostB;
    private SynchronizationMain.MODE updateModeA;
    private SynchronizationMain.MODE updateModeB;

    public ServerSynchronizationController(final SynchronizationMain.MODE updateModeA,
                                           final SynchronizationMain.MODE updateModeB,
                                           InetSocketAddress hostAddressA,
                                           InetSocketAddress hostAddressB,
                                           InetSocketAddress clientAddressA,
                                           InetSocketAddress clientAddressB) throws IOException {

        this.updateModeA = updateModeA;
        this.updateModeB = updateModeB;
        data = new SynchronizationMain.Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new SynchronizationGUI(hostMode);
        gui.setVisible(true);


        hostA = new DefaultHost<SynchronizationMain.Vector2D>(hostMode.toString(), hostAddressA, clientAddressA,
                SynchronizationMain.Vector2D.class, new DefaultHost.DataListener<SynchronizationMain.Vector2D>() {
            @Override
            public void handleOrderedData(final SynchronizationMain.Vector2D orderedData) {
                if (updateModeA == SynchronizationMain.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final SynchronizationMain.Vector2D newestData) {
                if (updateModeA == SynchronizationMain.MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });

        hostB = new DefaultHost<SynchronizationMain.Vector2D>(hostMode.toString(), hostAddressB, clientAddressB,
                SynchronizationMain.Vector2D.class, new DefaultHost.DataListener<SynchronizationMain.Vector2D>() {
            @Override
            public void handleOrderedData(final SynchronizationMain.Vector2D orderedData) {
                if (updateModeB == SynchronizationMain.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final SynchronizationMain.Vector2D newestData) {
                if (updateModeB == SynchronizationMain.MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });

    }

    @Override
    public void run() {
        try {
            // receive from A
            Queue<SynchronizationMain.Vector2D> receivedQueueA = hostA.receive();
            for (final SynchronizationMain.Vector2D receivedData : receivedQueueA)
                if (updateModeA == SynchronizationMain.MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // receive from B
            Queue<SynchronizationMain.Vector2D> receivedQueueB = hostB.receive();
            for (final SynchronizationMain.Vector2D receivedData : receivedQueueB)
                if (updateModeB == SynchronizationMain.MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // route from A to B
            for (final SynchronizationMain.Vector2D receivedData : receivedQueueA)
                 hostB.send(receivedData);

            // route from B to A
            for (final SynchronizationMain.Vector2D receivedData : receivedQueueB)
                hostA.send(receivedData);

            // send server to both
            gui.sendGUI(data);
            hostA.send(data);
            hostB.send(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SynchronizationGUI getGui() {
        return gui;
    }
}
