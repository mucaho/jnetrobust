/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;

import com.github.mucaho.jnetrobust.example.ProtocolHost;
import com.github.mucaho.jnetrobust.example.ProtocolHandle;
import com.github.mucaho.jnetrobust.example.ProtocolHandleListener;
import com.github.mucaho.jnetrobust.example.advanced.SynchronizationMain.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AbstractSynchronizationController {
    private final HOST hostMode;
    private final SynchronizationGUI gui;
    private final ProtocolHost host;

    AbstractSynchronizationController(HostInformation info) throws IOException {
        hostMode = info.hostMode;

        gui = new SynchronizationGUI(info.hostMode);
        gui.setVisible(true);

        host = new ProtocolHost(info.hostMode.toString(), info.localAddress, Vector2D.class);
    }

    SynchronizationHandle register(HandleInformation info, ProtocolHandleListener<Vector2D>... additionalHandleListeners) {
        List<ProtocolHandleListener<Vector2D>> listeners = new ArrayList<ProtocolHandleListener<Vector2D>>();
        listeners.add(new ModeGUIHandleListener(info.updateMode, gui));
        listeners.addAll(Arrays.asList(additionalHandleListeners));

        ProtocolHandle<Vector2D> handle =
                host.register(info.topic, info.remoteAddress, new CombinedHandleListener(listeners));
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
        private final ProtocolHandle<Vector2D> hostHandle;
        private final MODE updateMode;
        private final SynchronizationGUI gui;
        private final Vector2D data;

        private SynchronizationHandle(ProtocolHandle<Vector2D> hostHandle,
                                      MODE updateMode,
                                      HOST hostMode,
                                      SynchronizationGUI gui) {
            this.hostHandle = hostHandle;
            this.updateMode = updateMode;
            this.gui = gui;
            this.data = new Vector2D(0, 0, hostMode);
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
            if (receivedData != null && updateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                gui.updateGUI(receivedData);
            return receivedData;
        }

        public MODE getUpdateMode() {
            return updateMode;
        }
    }

    static class CombinedHandleListener implements ProtocolHandleListener<Vector2D> {
        private final List<ProtocolHandleListener<Vector2D>> listeners;

        CombinedHandleListener(List<ProtocolHandleListener<Vector2D>> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void handleOrderedData(Vector2D orderedData) {
            for (ProtocolHandleListener<Vector2D> listener : listeners)
                listener.handleOrderedData(orderedData);
        }

        @Override
        public void handleNewestData(Vector2D newestData) {
            for (ProtocolHandleListener<Vector2D> listener : listeners)
                listener.handleNewestData(newestData);
        }

        @Override
        public void handleExceptionalData(Exception exception) {
            exception.printStackTrace();
        }
    }

    static class ModeGUIHandleListener implements ProtocolHandleListener<Vector2D> {
        private final MODE updateMode;
        private final SynchronizationGUI gui;

        ModeGUIHandleListener(MODE updateMode, SynchronizationGUI gui) {
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

        @Override
        public void handleExceptionalData(Exception exception) {
            exception.printStackTrace();
        }
    }
}
