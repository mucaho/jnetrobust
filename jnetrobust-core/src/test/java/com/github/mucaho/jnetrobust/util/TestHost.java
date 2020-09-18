/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.Protocol;
import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.ProtocolListener;
import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.controller.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NavigableMap;


public class TestHost<T> implements Runnable {
    public interface TestHostListener<T> {
        void notifySent(T value);
        void notifyReceived(T value);
    }

    public interface TestHostDataGenerator<T> {
        T generateData();
        ByteBuffer serializeData(T value);
        T deserializeData(ByteBuffer data);
    }

    private final TestHostListener<T> hostListener;
    private final TestHostDataGenerator<T> dataGenerator;
    private final UnreliableQueue<Packet> inQueue;
    private final UnreliableQueue<Packet> outQueue;
    private final Protocol protocol;
    private final String debugName;


    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<Packet> inQueue, UnreliableQueue<Packet> outQueue,
                    ProtocolListener protocolListener, ProtocolConfig config, String debugName) {
        this.debugName = debugName;
        this.hostListener = hostListener;
        this.dataGenerator = dataGenerator;
        this.protocol = new Protocol(protocolListener, config);
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    private Packet networkClone(Packet outPacket) {
        if (outPacket == null) return null;

        Packet inPacket = null;

        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outStream);
            {
                outPacket.writeExternal(out);
            }
            out.close();

            ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
            ObjectInputStream in = new ObjectInputStream(inStream);
            {
                inPacket = new Packet();
                inPacket.readExternal(in);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inPacket;
    }

    public void receive() {
        Packet packet;
        while ((packet = networkClone(inQueue.poll())) != null) {
            NavigableMap<Short, ByteBuffer> datas = protocol.receive(packet);
            for (ByteBuffer data : datas.values()) {
                hostListener.notifyReceived(dataGenerator.deserializeData(data));
            }
        }
    }

    public void send() {
        ByteBuffer data = dataGenerator.serializeData(dataGenerator.generateData());
        NavigableMap<Short, Packet> packetMap = protocol.send(data);

        for (Packet packet : packetMap.values()) {
            List<Segment> segments = new ArrayList<Segment>(packet.getSegments());
            for (Segment segment : segments) {
                hostListener.notifySent(dataGenerator.deserializeData(segment.getData()));
            }

            outQueue.offer(networkClone(packet));
        }
    }


    @Override
    public void run() {
        Thread.currentThread().setName(debugName != null ? debugName : "");
        receive();
        send();
        if (debugName != null) {
            System.out.println("(" + debugName + ")" +
                    "\tE(X):\t" + protocol.getSmoothedRTT() +
                    "\tVar(X):\t" + protocol.getRTTVariation());
        }
        Thread.yield();
    }

}
