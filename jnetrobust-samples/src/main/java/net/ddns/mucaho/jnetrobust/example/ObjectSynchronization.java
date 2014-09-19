package net.ddns.mucaho.jnetrobust.example;


import net.ddns.mucaho.jnetemu.DatagramWanEmulator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ObjectSynchronization {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    private static InetSocketAddress SERVER_ADDRESS;
    private static InetSocketAddress CLIENT_ADDRESS;
    private static InetSocketAddress EMULATOR_ADDRESS;

    static {
        try {
            SERVER_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
            CLIENT_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12346);
            EMULATOR_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12347);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // client
        ObjectSynchronizationController client = new ObjectSynchronizationController(ObjectSynchronizationController.HOST.CLIENT,
                ObjectSynchronizationController.MODE.UPDATE_ON_ORDERED_DATA, CLIENT_ADDRESS, EMULATOR_ADDRESS);
        // server
        ObjectSynchronizationController server = new ObjectSynchronizationController(ObjectSynchronizationController.HOST.SERVER,
                ObjectSynchronizationController.MODE.UPDATE_ON_RECEIVED_DATA, SERVER_ADDRESS, EMULATOR_ADDRESS);
        // emulator emulates bad networking conditions as in WANs
        DatagramWanEmulator wanEmulator = new DatagramWanEmulator(EMULATOR_ADDRESS, CLIENT_ADDRESS, SERVER_ADDRESS);

        // start everything
        wanEmulator.startEmulation();
        executor.scheduleAtFixedRate(client, 60, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(server, 68, 16, TimeUnit.MILLISECONDS);
    }

}