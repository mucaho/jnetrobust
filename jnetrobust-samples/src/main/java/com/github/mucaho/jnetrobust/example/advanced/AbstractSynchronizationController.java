/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;

import com.github.mucaho.jnetrobust.example.DefaultHost;
import com.github.mucaho.jnetrobust.example.DefaultHost.*;
import com.github.mucaho.jnetrobust.example.advanced.SynchronizationMain.*;

import java.io.IOException;
import java.net.SocketAddress;


public class AbstractSynchronizationController {
    private final SynchronizationGUI gui;
    private final Vector2D data;
    private final DefaultHost<Vector2D> host;

    AbstractSynchronizationController(HostInformation info) throws IOException {
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, info.hostMode);

        gui = new SynchronizationGUI(info.hostMode);
        gui.setVisible(true);

        host = new DefaultHost<Vector2D>(info.hostMode.toString(), Vector2D.class, info.localAddress);
    }

    SynchronizationHandle register(HandleInformation info) {
        HostHandle<Vector2D> handle =
                host.register(info.topic, info.remoteAddress, new ModeDataListener(info.updateMode, gui));
        return new SynchronizationHandle(handle, info.updateMode, gui, data);
    }

    public SynchronizationGUI getGui() {
        return gui;
    }





    static class HostInformation {
        HOST hostMode;
        SocketAddress localAddress;

        HostInformation(HOST hostMode, SocketAddress localAddress) {
            this.hostMode = hostMode;
            this.localAddress = localAddress;
        }
    }

    static class HandleInformation {
        MODE updateMode;
        SocketAddress remoteAddress;
        byte topic;

        HandleInformation(MODE updateMode, SocketAddress remoteAddress, byte topic) {
            this.updateMode = updateMode;
            this.remoteAddress = remoteAddress;
            this.topic = topic;
        }
    }




    static class SynchronizationHandle {
        private final HostHandle<Vector2D> hostHandle;
        private final MODE udpdateMode;
        private final SynchronizationGUI gui;
        private final Vector2D data;

        private SynchronizationHandle(HostHandle<Vector2D> hostHandle, MODE updateMode,
                                      SynchronizationGUI gui, Vector2D data) {
            this.hostHandle = hostHandle;
            this.udpdateMode = updateMode;
            this.gui = gui;
            this.data = data;
        }

        public void send() throws IOException {
            gui.sendGUI(data);
            hostHandle.send(data);
        }

        public void send(Vector2D data) throws IOException {
            hostHandle.send(data);
        }

        public Vector2D receive() throws IOException, ClassNotFoundException {
            Vector2D receivedData = hostHandle.receive();
            if (receivedData != null && udpdateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                gui.updateGUI(receivedData);
            return receivedData;
        }
    }

    static class ModeDataListener implements DefaultHost.DataListener<Vector2D> {
        private final MODE updateMode;
        private final SynchronizationGUI gui;

        ModeDataListener(MODE updateMode, SynchronizationGUI gui) {
            this.updateMode = updateMode;
            this.gui = gui;
        }

        @Override
        public void handleOrderedData(Vector2D orderedData) {
            if (updateMode == MODE.UPDATE_ON_ORDERED_DATA)
                gui.updateGUI(orderedData);
        }

        @Override
        public void handleNewestData(Vector2D newestData) {
            if (updateMode == MODE.UPDATE_ON_NEWEST_DATA)
                gui.updateGUI(newestData);
        }
    }
}
