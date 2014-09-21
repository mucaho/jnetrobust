package net.ddns.mucaho.jnetrobust.example;


import net.ddns.mucaho.jnetemu.DatagramWanEmulator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ObjectSynchronization {
    public static enum HOST {CLIENTA, SERVER, CLIENTB}
    public static enum MODE {UPDATE_ON_RECEIVED_DATA, UPDATE_ON_NEWEST_DATA, UPDATE_ON_ORDERED_DATA}

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

    private static InetSocketAddress SERVERA_ADDRESS;
    private static InetSocketAddress SERVERB_ADDRESS;
    private static InetSocketAddress CLIENTA_ADDRESS;
    private static InetSocketAddress CLIENTB_ADDRESS;
    private static InetSocketAddress EMULATORA_ADDRESS;
    private static InetSocketAddress EMULATORB_ADDRESS;

    static {
        try {
            SERVERA_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
            SERVERB_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12346);
            CLIENTA_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12347);
            CLIENTB_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12348);
            EMULATORA_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12349);
            EMULATORB_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12350);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // clientA
        ObjectSynchronizationController clientA = new ObjectSynchronizationController(HOST.CLIENTA,
                MODE.UPDATE_ON_RECEIVED_DATA, CLIENTA_ADDRESS, EMULATORA_ADDRESS);
        // clientB
        ObjectSynchronizationController clientB = new ObjectSynchronizationController(HOST.CLIENTB,
                MODE.UPDATE_ON_ORDERED_DATA, CLIENTB_ADDRESS, EMULATORB_ADDRESS);
        // server
        ServerObjectSynchronizationController server = new ServerObjectSynchronizationController(
                MODE.UPDATE_ON_RECEIVED_DATA, MODE.UPDATE_ON_NEWEST_DATA,
                SERVERA_ADDRESS, SERVERB_ADDRESS,
                EMULATORA_ADDRESS, EMULATORB_ADDRESS);

        // emulators emulate bad networking conditions as in WANs
        DatagramWanEmulator wanEmulatorA = new DatagramWanEmulator(EMULATORA_ADDRESS, CLIENTA_ADDRESS, SERVERA_ADDRESS);
        DatagramWanEmulator wanEmulatorB = new DatagramWanEmulator(EMULATORB_ADDRESS, CLIENTB_ADDRESS, SERVERB_ADDRESS);

        // start everything
        wanEmulatorA.startEmulation();
        wanEmulatorB.startEmulation();
        executor.scheduleAtFixedRate(clientA, 68, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(clientB, 68, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(server, 60, 16, TimeUnit.MILLISECONDS);
    }


    public static class Vector2D {
        private HOST host;
        private int x;
        private int y;

        public Vector2D() {
        }

        public Vector2D(int x, int y, HOST host) {
            this.x = x;
            this.y = y;
            this.host = host;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public HOST getHost() {
            return host;
        }

        public void setHost(HOST host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return "Vector2D{" +
                    "host=" + host +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}