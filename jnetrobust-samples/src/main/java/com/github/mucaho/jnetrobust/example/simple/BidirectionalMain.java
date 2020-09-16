/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.simple;


import com.github.mucaho.jnetrobust.example.ProtocolHost;
import com.github.mucaho.jnetrobust.example.ProtocolHostHandle;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * This is the most basic example.
 * Two hosts communicate with each other in an environment with ideal network conditions.
 * They send each other <code>String</code> messages.
 */
public class BidirectionalMain {
    public static void main (String[] args) throws Exception {
        String receivedMessage;

        // host addresses
        InetSocketAddress ADDRESS_A = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
        InetSocketAddress ADDRESS_B = new InetSocketAddress(InetAddress.getLocalHost(), 12346);

        // setup ProtocolHosts using the host's local address and registering all serialization dataTypes
        // ProtocolHost supports multiplexing between different peers using respective topicId, remote address and dataType

        ProtocolHost protocolHostA = new ProtocolHost("A", ADDRESS_A, String.class);
        ProtocolHostHandle<String> hostHandleA = protocolHostA.register(Byte.MIN_VALUE, ADDRESS_B);

        ProtocolHost protocolHostB = new ProtocolHost("B", ADDRESS_B, String.class);
        ProtocolHostHandle<String> hostHandleB = protocolHostB.register(Byte.MIN_VALUE, ADDRESS_A);


        // send from A
        hostHandleA.send(Arrays.asList("Hi!", "How you doing?"));

        System.out.println();
        Thread.sleep(100);

        // receive at B
        while ((receivedMessage = hostHandleB.receive()) != null) {
            System.out.println("<B>\t"+receivedMessage);
        }

        // send from B
        hostHandleB.send("Howdy! Fine, thanks.");

        System.out.println();
        Thread.sleep(100);

        // receive at A
        while ((receivedMessage = hostHandleA.receive()) != null) {
            System.out.println("<A>\t"+receivedMessage);
        }
    }
}
