/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.simple;

import com.github.mucaho.jnetemu.DatagramWanEmulator;
import com.github.mucaho.jnetrobust.example.ProtocolHost;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This examples demonstrates unidirectional communication between a sender that sends data and
 * a receiver that just acknowledges the receipt of data.
 * The impact of the emulated, bad network conditions is shown in the output.
 */
public class UnidirectionalMain {
    private final static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    private static InetSocketAddress senderAddress;
    private static InetSocketAddress emulatorAddress;
    private static InetSocketAddress receiverAddress;
    static {
        try {
            senderAddress = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
            emulatorAddress = new InetSocketAddress(InetAddress.getLocalHost(), 12346);
            receiverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 12347);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    private static class Sender implements Runnable {
        private long counter = 0L;
        private ProtocolHost.ProtocolHandle<Long> senderHandle;

        private Sender() throws IOException {
            ProtocolHost senderHost = new ProtocolHost(null, senderAddress, Long.class);
            senderHandle = senderHost.register(Byte.MIN_VALUE, emulatorAddress);
        }
        @Override
        public void run() {
            try {
                // receive incoming acknowledgements
                while (senderHandle.receive() != null) ;

                // send acknowledgements & data
                senderHandle.send(counter++);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Receiver implements Runnable {
        private ProtocolHost.ProtocolHandle<Long> receiverHandle;

        private Receiver() throws IOException {
            ProtocolHost receiverHost = new ProtocolHost(null, receiverAddress, Long.class);
            receiverHandle = receiverHost.register(Byte.MIN_VALUE, emulatorAddress);
        }

        @Override
        public void run() {
            try {
                // receive incoming acknowledgements & data
                Long receivedData;
                while ((receivedData = receiverHandle.receive()) != null) {
                    System.out.println(receivedData);
                }

                // send acknowledgements of received messages
                receiverHandle.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Sender sender = new Sender();
        Receiver receiver = new Receiver();
        // emulate network characteristics
        DatagramWanEmulator netEmulator = new DatagramWanEmulator(emulatorAddress, senderAddress, receiverAddress);

        // start sending/receiving @60Hz
        netEmulator.startEmulation();
        executor.scheduleAtFixedRate(sender, 0, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(receiver, 8, 16, TimeUnit.MILLISECONDS);

        // stop everything after 1000 ms
        Thread.sleep(1000);
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        netEmulator.stopEmulation();
    }
}
