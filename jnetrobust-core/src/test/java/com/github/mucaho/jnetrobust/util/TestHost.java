/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.Protocol;
import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.controller.Packet;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NavigableMap;


public class TestHost<T> implements Runnable {
    public interface TestHostListener<T> {
        public void notifySent(T value);
        public void notifyReceived(T value);
    }

    public interface TestHostDataGenerator<T> {
        public T generateData();
    }

    private final TestHostListener<T> hostListener;
    private final TestHostDataGenerator<T> dataGenerator;
    private final UnreliableQueue<Packet<T>> inQueue;
    private final UnreliableQueue<Packet<T>> outQueue;
    private final Protocol<T> protocol;
    private final String debugName;


    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<Packet<T>> inQueue, UnreliableQueue<Packet<T>> outQueue,
                    ProtocolConfig<T> config, String debugName) {
        this.debugName = debugName;
        this.hostListener = hostListener;
        this.dataGenerator = dataGenerator;
        this.protocol = new Protocol<T>(config);
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    private Packet<T> networkClone(Packet<T> outPacket) {
        if (outPacket == null) return null;

        Packet<T> inPacket = null;

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
                inPacket = new Packet<T>();
                inPacket.readExternal(in);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inPacket;
    }

    public void receive() {
        Packet<T> packet;
        while ((packet = networkClone(inQueue.poll())) != null) {
            NavigableMap<Short, T> datas = protocol.receive(networkClone(packet));
            for (T data: datas.values()) {
                hostListener.notifyReceived(data);
            }
        }
    }

    public void send() {
        T data = dataGenerator.generateData();
        Packet<T> packet = networkClone(protocol.send(data).getValue());

        List<Metadata<T>> metadatas = new ArrayList<Metadata<T>>(packet.getMetadatas());
        for (Metadata<T> metadata : metadatas) {
            hostListener.notifySent(metadata.getData());
        }

        outQueue.offer(networkClone(packet));
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
    }

}
