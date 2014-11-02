/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.simple;


import com.github.mucaho.jnetrobust.example.DefaultHost;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class BidirectionalMain {
    public static void main (String[] args) throws Exception {
        String receivedMessage;

        // host addresses
        InetSocketAddress ADDRESS_A = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
        InetSocketAddress ADDRESS_B = new InetSocketAddress(InetAddress.getLocalHost(), 12346);

        // setup DefaultHost A
        DefaultHost<String> hostA = new DefaultHost<String>("A", String.class, ADDRESS_A);
        DefaultHost.HostHandle<String> hostHandleA = hostA.register(Byte.MIN_VALUE, ADDRESS_B);

        // setup DefaultHost B
        DefaultHost<String> hostB = new DefaultHost<String>("B", String.class, ADDRESS_B);
        DefaultHost.HostHandle<String> hostHandleB = hostB.register(Byte.MIN_VALUE, ADDRESS_A);


        // send from A
        hostHandleA.send("Hi!");
        hostHandleA.send("How you doing?");

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
