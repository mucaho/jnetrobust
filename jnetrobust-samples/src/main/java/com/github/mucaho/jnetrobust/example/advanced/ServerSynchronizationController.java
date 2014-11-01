/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


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

        clientAHandle = register(handleDirectAInfo);
        relayBHandle = register(handleRelayBInfo);
        clientBHandle = register(handleDirectBInfo);
        relayAHandle = register(handleRelayAInfo);
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
                relayBHandle.send(receivedData);
            }

            // receive from B
            while ((receivedData = clientBHandle.receive()) != null) {
                // relay to A
                relayAHandle.send(receivedData);
            }

            // send server to both
            clientAHandle.send();
            clientBHandle.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
