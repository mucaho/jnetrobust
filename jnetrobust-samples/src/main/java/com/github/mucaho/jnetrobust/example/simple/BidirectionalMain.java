/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.simple;


import com.github.mucaho.jnetrobust.example.ProtocolHost;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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

        // setup ProtocolHost A
        ProtocolHost<String> protocolHostA = new ProtocolHost<String>("A", String.class, ADDRESS_A);
        ProtocolHost.ProtocolHandle<String> protocolHandleA = protocolHostA.register(Byte.MIN_VALUE, ADDRESS_B);

        // setup ProtocolHost B
        ProtocolHost<String> protocolHostB = new ProtocolHost<String>("B", String.class, ADDRESS_B);
        ProtocolHost.ProtocolHandle<String> protocolHandleB = protocolHostB.register(Byte.MIN_VALUE, ADDRESS_A);


        // send from A
        protocolHandleA.send("Hi!");
        protocolHandleA.send("How you doing?");

        System.out.println();
        Thread.sleep(100);

        // receive at B
        while ((receivedMessage = protocolHandleB.receive()) != null) {
            System.out.println("<B>\t"+receivedMessage);
        }

        // send from B
        protocolHandleB.send("Howdy! Fine, thanks.");

        System.out.println();
        Thread.sleep(100);

        // receive at A
        while ((receivedMessage = protocolHandleA.receive()) != null) {
            System.out.println("<A>\t"+receivedMessage);
        }
    }
}
