/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


import com.github.mucaho.jnetrobust.example.advanced.SynchronizationMain.Vector2D;

import java.io.IOException;


public class ClientSynchronizationController extends AbstractSynchronizationController implements Runnable {
    private final SynchronizationHandle relayHandle;
    private final SynchronizationHandle directHandle;

    public ClientSynchronizationController(HostInformation hostInfo, HandleInformation handleDirectInfo,
                                           HandleInformation handleRelayInfo) throws IOException {
        super(hostInfo);
        relayHandle = register(handleRelayInfo);
        directHandle = register(handleDirectInfo);
    }

    @Override
    public void run() {
        Vector2D receivedData;
        try {
            // receive relayed data from server
            while (relayHandle.receive() != null) ;

            // receive server data
            while (directHandle.receive() != null) ;

            // send relay acknowledgements to server
            relayHandle.send(null);

            // send client to server
            directHandle.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}