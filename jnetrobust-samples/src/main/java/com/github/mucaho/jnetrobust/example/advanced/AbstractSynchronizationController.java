/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;

import com.github.mucaho.jnetrobust.example.ProtocolHost;
import com.github.mucaho.jnetrobust.example.ProtocolHost.*;
import com.github.mucaho.jnetrobust.example.advanced.SynchronizationMain.*;

import java.io.IOException;
import java.net.SocketAddress;


public class AbstractSynchronizationController {
    private final SynchronizationGUI gui;
    private final Vector2D data;
    private final ProtocolHost<Vector2D> host;

    AbstractSynchronizationController(HostInformation info) throws IOException {
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, info.hostMode);

        gui = new SynchronizationGUI(info.hostMode);
        gui.setVisible(true);

        host = new ProtocolHost<Vector2D>(info.hostMode.toString(), Vector2D.class, info.localAddress);
    }

    SynchronizationHandle register(HandleInformation info) {
        ProtocolHandle<Vector2D> handle =
                host.register(info.topic, info.remoteAddress, new ModeDataListener(info.updateMode, gui));
        return new SynchronizationHandle(handle, info.updateMode, gui, data);
    }

    SynchronizationGUI getGui() {
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
        private final ProtocolHandle<Vector2D> protocolHandle;
        private final MODE udpdateMode;
        private final SynchronizationGUI gui;
        private final Vector2D data;

        private SynchronizationHandle(ProtocolHandle<Vector2D> protocolHandle, MODE updateMode,
                                      SynchronizationGUI gui, Vector2D data) {
            this.protocolHandle = protocolHandle;
            this.udpdateMode = updateMode;
            this.gui = gui;
            this.data = data;
        }

        public void send() throws IOException {
            gui.sendGUI(data);
            protocolHandle.send(data);
        }

        public void send(Vector2D data) throws IOException {
            protocolHandle.send(data);
        }

        public Vector2D receive() throws IOException, ClassNotFoundException {
            Vector2D receivedData = protocolHandle.receive();
            if (receivedData != null && udpdateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                gui.updateGUI(receivedData);
            return receivedData;
        }
    }

    static class ModeDataListener implements ProtocolHost.DataListener<Vector2D> {
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
