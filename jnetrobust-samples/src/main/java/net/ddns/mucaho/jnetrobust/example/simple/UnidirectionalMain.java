package net.ddns.mucaho.jnetrobust.example.simple;

import net.ddns.mucaho.jnetemu.DatagramWanEmulator;
import net.ddns.mucaho.jnetrobust.example.DefaultHost;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        private DefaultHost<Long> senderHost;

        private Sender() throws IOException {
            senderHost = new DefaultHost<Long>(null, senderAddress, emulatorAddress,
                    Long.class, new DefaultHost.DataListener<Long>() {
                @Override
                public void handleOrderedData(Long orderedData) {
                }
                @Override
                public void handleNewestData(Long newestData) {
                }
            });
        }
        @Override
        public void run() {
            try {
                // receive incoming acknowledgements
                senderHost.receive();

                // send acknowledgements & data
                senderHost.send(counter++);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Receiver implements Runnable {
        private DefaultHost<Long> receiverHost;

        private Receiver() throws IOException {
            receiverHost = new DefaultHost<Long>(null, receiverAddress, emulatorAddress,
                    Long.class, new DefaultHost.DataListener<Long>() {
                @Override
                public void handleOrderedData(Long orderedData) {
                }
                @Override
                public void handleNewestData(Long newestData) {
                }
            });
        }
        @Override
        public void run() {
            try {
                // receive incoming acknowledgements & data
                Queue<Long> receivedDatas = receiverHost.receive();
                for (Long receivedData: receivedDatas)
                    System.out.println(receivedData);

                // send acknowledgements of received messages only
                receiverHost.send();

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
