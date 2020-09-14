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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AbstractSynchronizationController {
    private final HOST hostMode;
    private final SynchronizationGUI gui;
    private final ProtocolHost<Vector2D> host;

    AbstractSynchronizationController(HostInformation info) throws IOException {
        hostMode = info.hostMode;

        gui = new SynchronizationGUI(info.hostMode);
        gui.setVisible(true);

        host = new ProtocolHost<Vector2D>(info.hostMode.toString(), Vector2D.class, info.localAddress);
    }

    SynchronizationHandle register(HandleInformation info, ProtocolHost.DataListener<Vector2D>... additionalDataListeners) {
        List<ProtocolHost.DataListener<Vector2D>> listeners = new ArrayList<DataListener<Vector2D>>();
        listeners.add(new ModeGUIDataListener(info.updateMode, gui));
        listeners.addAll(Arrays.asList(additionalDataListeners));

        ProtocolHandle<Vector2D> handle =
                host.register(info.topic, info.remoteAddress, new CombinedDataListener(listeners));
        return new SynchronizationHandle(handle, info.updateMode, hostMode, gui);
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
        private final MODE updateMode;
        private final HOST hostMode;
        private final SynchronizationGUI gui;

        private SynchronizationHandle(ProtocolHandle<Vector2D> protocolHandle,
                                      MODE updateMode,
                                      HOST hostMode,
                                      SynchronizationGUI gui) {
            this.protocolHandle = protocolHandle;
            this.updateMode = updateMode;
            this.hostMode = hostMode;
            this.gui = gui;
        }

        public void send() throws IOException {
            Vector2D data = new Vector2D();
            data.setHost(hostMode);
            gui.sendGUI(data);
            protocolHandle.send(data);
        }

        public void send(Vector2D data) throws IOException {
            protocolHandle.send(data);
        }

        public Vector2D receive() throws IOException, ClassNotFoundException {
            Vector2D receivedData = protocolHandle.receive();
            if (receivedData != null && updateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                gui.updateGUI(receivedData);
            return receivedData;
        }

        public MODE getUpdateMode() {
            return updateMode;
        }
    }

    static class CombinedDataListener implements ProtocolHost.DataListener<Vector2D> {
        private final List<DataListener<Vector2D>> listeners;

        CombinedDataListener(List<DataListener<Vector2D>> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void handleOrderedData(Vector2D orderedData) {
            for (ProtocolHost.DataListener<Vector2D> listener : listeners)
                listener.handleOrderedData(orderedData);
        }

        @Override
        public void handleNewestData(Vector2D newestData) {
            for (ProtocolHost.DataListener<Vector2D> listener : listeners)
                listener.handleNewestData(newestData);
        }
    }

    static class ModeGUIDataListener implements ProtocolHost.DataListener<Vector2D> {
        private final MODE updateMode;
        private final SynchronizationGUI gui;

        ModeGUIDataListener(MODE updateMode, SynchronizationGUI gui) {
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
