/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


import com.github.mucaho.jnetrobust.example.ProtocolHandleListener;
import com.github.mucaho.jnetrobust.example.advanced.SynchronizationMain.*;

import java.io.IOException;

public class ServerSynchronizationController extends AbstractSynchronizationController implements Runnable {
    private SynchronizationHandle clientAHandle;
    private SynchronizationHandle relayBHandle;
    private SynchronizationHandle clientBHandle;
    private SynchronizationHandle relayAHandle;

    public ServerSynchronizationController(HostInformation hostInfo,
                                           HandleInformation handleDirectAInfo,
                                           HandleInformation handleRelayBInfo,
                                           HandleInformation handleDirectBInfo,
                                           HandleInformation handleRelayAInfo) throws IOException {
        super(hostInfo);

        ModeRelayHandleListener relayBDataListener = new ModeRelayHandleListener(handleRelayBInfo.updateMode);
        clientAHandle = register(handleDirectAInfo, relayBDataListener);
        relayBHandle = register(handleRelayBInfo);
        relayBDataListener.setHandle(relayBHandle);

        ModeRelayHandleListener relayADataListener = new ModeRelayHandleListener(handleRelayAInfo.updateMode);
        clientBHandle = register(handleDirectBInfo, relayADataListener);
        relayAHandle = register(handleRelayAInfo);
        relayADataListener.setHandle(relayAHandle);

        getGui().addDescription("From " + HOST.CLIENTA.toString() + ": " + handleDirectAInfo.updateMode);
        getGui().addDescription("From " + HOST.CLIENTB.toString() + ": " + handleDirectBInfo.updateMode);
    }

    @Override
    public void run() {
        Vector2D receivedData;
        try {
            // receive acknowledgements from relays
            while (relayAHandle.receive() != null) ;
            while (relayBHandle.receive() != null) ;

            // receive from A
            while ((receivedData = clientAHandle.receive()) != null) {
                // relay to B
                if (relayBHandle.getUpdateMode() == MODE.UPDATE_ON_RECEIVED_DATA) {
                    relayBHandle.send(receivedData);
                }
            }

            // receive from B
            while ((receivedData = clientBHandle.receive()) != null) {
                // relay to A
                if (relayAHandle.getUpdateMode() == MODE.UPDATE_ON_RECEIVED_DATA) {
                    relayAHandle.send(receivedData);
                }
            }

            // send server to both
            clientAHandle.send();
            clientBHandle.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ModeRelayHandleListener implements ProtocolHandleListener<Vector2D> {
        private final MODE updateMode;
        private SynchronizationHandle handle;

        public ModeRelayHandleListener(MODE updateMode) {
            this.updateMode = updateMode;
        }

        public void setHandle(SynchronizationHandle handle) {
            this.handle = handle;
        }

        @Override
        public void handleOrderedData(Vector2D orderedData) {
            if (updateMode == MODE.UPDATE_ON_ORDERED_DATA) {
                try {
                    handle.send(orderedData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleNewestData(Vector2D newestData) {
            if (updateMode == MODE.UPDATE_ON_NEWEST_DATA) {
                try {
                    handle.send(newestData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleExceptionalData(Exception exception) {
            exception.printStackTrace();
        }
    }
}
