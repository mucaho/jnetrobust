/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


import com.github.mucaho.jnetrobust.example.DefaultHost;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;


public class ClientSynchronizationController implements Runnable {

    private SynchronizationGUI gui;
    private DefaultHost<SynchronizationMain.Vector2D> host;
    private final SynchronizationMain.MODE updateMode;
    private final SynchronizationMain.Vector2D data;

    public ClientSynchronizationController(final SynchronizationMain.HOST hostMode, final SynchronizationMain.MODE updateMode,
                                           InetSocketAddress hostAddress,
                                           InetSocketAddress receiverAddress) throws IOException {

        this.updateMode = updateMode;
        data = new SynchronizationMain.Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new SynchronizationGUI(hostMode);
        gui.setVisible(true);


        host = new DefaultHost<SynchronizationMain.Vector2D>(hostMode.toString(), hostAddress, receiverAddress,
                SynchronizationMain.Vector2D.class, new DefaultHost.DataListener<SynchronizationMain.Vector2D>() {
            @Override
            public void handleOrderedData(final SynchronizationMain.Vector2D orderedData) {
                if (updateMode == SynchronizationMain.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }

            @Override
            public void handleNewestData(final SynchronizationMain.Vector2D newestData) {
                if (updateMode == SynchronizationMain.MODE.UPDATE_ON_NEWEST_DATA)
                    gui.updateGUI(newestData);
            }
        });
    }



    @Override
    public void run() {
        try {
            // receive
            Queue<SynchronizationMain.Vector2D> receivedQueue = host.receive();
            for (final SynchronizationMain.Vector2D receivedData : receivedQueue)
                if (updateMode == SynchronizationMain.MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // send
            gui.sendGUI(data);
            host.send(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public SynchronizationGUI getGui() {
        return gui;
    }
}