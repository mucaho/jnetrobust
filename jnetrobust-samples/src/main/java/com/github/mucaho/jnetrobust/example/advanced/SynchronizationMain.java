/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.mucaho.jnetrobust.example.advanced;


import com.github.mucaho.jnetemu.DatagramWanEmulator;
import com.github.mucaho.jnetrobust.example.advanced.AbstractSynchronizationController.*;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * This is an advanced example showcasing two clients and a server communicating the position of their controlled object to each other.
 * The communication is once again affected by emulated, bad network conditions.
 * <br /><br />
 * The server is relaying communication between the clients.
 * Remotely controlled objects are displayed using different synchronization strategies:
 * <ul>
 *     <li><code>UPDATE_TO_NEWEST_POSITION</code></li>
 *     <li><code>UPDATE_TO_ORDERED_POSITION</code></li>
 *     <li><code>UPDATE_TO_RECEIVED_POSITION</code></li>
 * </ul>
 * Focus a window and use the arrow keys to control that host's object.
 */
public class SynchronizationMain {

    public static enum HOST {CLIENTA, SERVER, CLIENTB}
    private final static byte topicA = Byte.MIN_VALUE;
    private final static byte topicB = Byte.MAX_VALUE;

    public static enum MODE {UPDATE_ON_RECEIVED_DATA, UPDATE_ON_NEWEST_DATA, UPDATE_ON_ORDERED_DATA}

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

    private static InetSocketAddress SERVER_ADDRESS;
    private static InetSocketAddress CLIENTA_ADDRESS;
    private static InetSocketAddress CLIENTB_ADDRESS;
    private static InetSocketAddress EMULATORA_ADDRESS;
    private static InetSocketAddress EMULATORB_ADDRESS;
    static {
        try {
            CLIENTA_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12340);
            EMULATORA_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
            SERVER_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12350);
            EMULATORB_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12355);
            CLIENTB_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 12360);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        // clientA
        ClientSynchronizationController clientA = new ClientSynchronizationController(
                new HostInformation(HOST.CLIENTA, CLIENTA_ADDRESS),
                new HandleInformation(MODE.UPDATE_ON_ORDERED_DATA, EMULATORA_ADDRESS, topicA),
                new HandleInformation(MODE.UPDATE_ON_RECEIVED_DATA, EMULATORA_ADDRESS, topicB));

        // clientB
        ClientSynchronizationController clientB = new ClientSynchronizationController(
                new HostInformation(HOST.CLIENTB, CLIENTB_ADDRESS),
                new HandleInformation(MODE.UPDATE_ON_NEWEST_DATA, EMULATORB_ADDRESS, topicB),
                new HandleInformation(MODE.UPDATE_ON_ORDERED_DATA, EMULATORB_ADDRESS, topicA));

        // server
        ServerSynchronizationController server = new ServerSynchronizationController(
                new HostInformation(HOST.SERVER, SERVER_ADDRESS),
                new HandleInformation(MODE.UPDATE_ON_RECEIVED_DATA, EMULATORA_ADDRESS, topicA),
                new HandleInformation(MODE.UPDATE_ON_ORDERED_DATA, EMULATORB_ADDRESS, topicA),
                new HandleInformation(MODE.UPDATE_ON_NEWEST_DATA, EMULATORB_ADDRESS, topicB),
                new HandleInformation(MODE.UPDATE_ON_ORDERED_DATA, EMULATORA_ADDRESS, topicB));


        // emulators emulate bad networking conditions as in WANs
        DatagramWanEmulator wanEmulatorA = new DatagramWanEmulator(EMULATORA_ADDRESS, CLIENTA_ADDRESS, SERVER_ADDRESS);
        DatagramWanEmulator wanEmulatorB = new DatagramWanEmulator(EMULATORB_ADDRESS, CLIENTB_ADDRESS, SERVER_ADDRESS);

        // start everything
        wanEmulatorA.startEmulation();
        wanEmulatorB.startEmulation();
        executor.scheduleAtFixedRate(clientA, 68, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(clientB, 68, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(server, 60, 16, TimeUnit.MILLISECONDS);
    }


    public static class Vector2D implements Serializable {
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