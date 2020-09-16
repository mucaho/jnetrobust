/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;

import com.github.mucaho.jnetrobust.example.ProtocolHost;
import com.github.mucaho.jnetrobust.example.ProtocolHostHandle;
import com.github.mucaho.jnetrobust.example.ProtocolHostListener;
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

    SynchronizationHandle register(HandleInformation info, ProtocolHostListener<Vector2D>... additionalProtocolHostListeners) {
        List<ProtocolHostListener<Vector2D>> listeners = new ArrayList<ProtocolHostListener<Vector2D>>();
        listeners.add(new ModeGUIHostListener(info.updateMode, gui));
        listeners.addAll(Arrays.asList(additionalProtocolHostListeners));

        ProtocolHostHandle<Vector2D> handle =
                host.register(info.topic, info.remoteAddress, new CombinedHostListener(listeners));
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
        private final ProtocolHostHandle<Vector2D> hostHandle;
        private final MODE updateMode;
        private final SynchronizationGUI gui;
        private final Vector2D data;

        private SynchronizationHandle(ProtocolHostHandle<Vector2D> hostHandle,
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

    static class CombinedHostListener implements ProtocolHostListener<Vector2D> {
        private final List<ProtocolHostListener<Vector2D>> listeners;

        CombinedHostListener(List<ProtocolHostListener<Vector2D>> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void handleOrderedData(Vector2D orderedData) {
            for (ProtocolHostListener<Vector2D> listener : listeners)
                listener.handleOrderedData(orderedData);
        }

        @Override
        public void handleNewestData(Vector2D newestData) {
            for (ProtocolHostListener<Vector2D> listener : listeners)
                listener.handleNewestData(newestData);
        }

        @Override
        public void handleExceptionalData(Vector2D exceptionalData) {
        }
    }

    static class ModeGUIHostListener implements ProtocolHostListener<Vector2D> {
        private final MODE updateMode;
        private final SynchronizationGUI gui;

        ModeGUIHostListener(MODE updateMode, SynchronizationGUI gui) {
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
        public void handleExceptionalData(Vector2D exceptionalData) {
        }
    }
}
