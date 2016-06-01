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
import java.util.NavigableMap;
import java.util.function.Consumer;

public class TestHost<T> implements Runnable {
    public interface TestHostListener<T> {
        public void notifySent(T value);
        public void notifyRetransmitted(T value);
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


    public void receive() {
        Packet<T> packet;
        while ((packet = inQueue.poll()) != null) {
            NavigableMap<Short, T> datas = protocol.receive(packet);
            for (T data : datas.values()) {
                hostListener.notifyReceived(data);
            }
        }
    }

    public void send() {
        T data = dataGenerator.generateData();
        Packet<T> packet = protocol.send(data).getValue();

        List<Metadata<T>> metadatas = new ArrayList<Metadata<T>>(packet.getMetadatas());
        for (int i = 0; i < metadatas.size(); ++i) {
            if (i < metadatas.size() - 1)
                hostListener.notifyRetransmitted(metadatas.get(i).getData());
            hostListener.notifySent(metadatas.get(i).getData());
        }

        outQueue.offer(packet);
    }


    @Override
    public void run() {
        receive();
        send();
        if (debugName != null) {
            System.out.println("(" + debugName + ")" +
                    "\tE(X):\t" + protocol.getSmoothedRTT() +
                    "\tVar(X):\t" + protocol.getRTTVariation());
        }
    }




}
