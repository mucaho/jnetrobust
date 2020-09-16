/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoObjectInput;
import com.esotericsoftware.kryo.io.KryoObjectOutput;
import com.github.mucaho.jnetrobust.Logger;
import com.github.mucaho.jnetrobust.Protocol;
import com.github.mucaho.jnetrobust.ProtocolListener;
import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.example.ProtocolHandle.ProtocolId;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstracts away much of the protocol's functionality with a default configuration,
 * and sets-up a <code>DatagramChannel</code> for sending/receiving data,
 * as well as a <code>Kryo</code> instance for serialization.
 * <br />
 * This is the preferred way to get your feet wet and be introduced to the protocol.
 * <br />
 * See the various examples for usage scenarios.
 */
public class ProtocolHost {

    private static final int IPTOS_LOWCOST = 0x02;
    private static final int IPTOS_RELIABILITY = 0x04;
    private static final int IPTOS_THROUGHPUT = 0x08;
    private static final int IPTOS_LOWDELAY = 0x10;

    private final String hostName;

    // protocol fields
    private final Map<ProtocolId, Protocol<?>> protocols = new ConcurrentHashMap<ProtocolId, Protocol<?>>();
    private final Map<ProtocolId, ProtocolHandleListener<?>> listeners = new ConcurrentHashMap<ProtocolId, ProtocolHandleListener<?>>();

    // serialization fields
    private final Kryo kryo;

    private final ByteBuffer buffer = ByteBuffer.allocate(4096);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    private final ByteBuffer bufferForCloning = ByteBuffer.allocate(4096);
    private final ByteBufferInput bufferInputForCloning = new ByteBufferInput();
    private final ByteBufferOutput bufferOutputForCloning = new ByteBufferOutput();
    private final KryoObjectInput objectInputForCloning;
    private final KryoObjectOutput objectOutputForCloning;

    // network communication fields
    private final DatagramChannel channel;

    public ProtocolHost(String hostName, SocketAddress localAddress, Class<? extends Serializable>... dataClasses) throws IOException {
        // setup network communication
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().setTrafficClass(IPTOS_LOWDELAY | IPTOS_THROUGHPUT | IPTOS_RELIABILITY);
        channel.socket().bind(localAddress);

        // setup serialization
        kryo = new Kryo();
        kryo.register(Packet.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(Segment.class); // add argument `new ExternalizableSerializer()` if needed
        for (Class<? extends Serializable> dataClass : dataClasses)
            kryo.register(dataClass);

        objectInput = new KryoObjectInput(kryo, bufferInput);
        objectOutput = new KryoObjectOutput(kryo, bufferOutput);

        objectInputForCloning = new KryoObjectInput(kryo, bufferInputForCloning);
        objectOutputForCloning = new KryoObjectOutput(kryo, bufferOutputForCloning);

        this.hostName = hostName;
    }

    public <T extends Serializable> ProtocolHandle<T> register(byte topic, SocketAddress remoteAddress) {
        return register(topic, remoteAddress, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> ProtocolHandle<T> register(byte topic, SocketAddress remoteAddress, final ProtocolHandleListener<T> listener) {
        final ProtocolId protocolId = new ProtocolId(topic, remoteAddress);

        if (listener != null)
            listeners.put(protocolId, listener);

        ProtocolListener<T> protocolListener = new ProtocolListener<T>() {
            @Override
            public void handleOrderedData(short dataId, T orderedData) {
                Queue<T> orderedQueue = (Queue<T>) orderedQueues.get(protocolId);
                if (orderedQueue == null) {
                    orderedQueue = new LinkedList<T>();
                    orderedQueues.put(protocolId, orderedQueue);
                }
                orderedQueue.offer(serializationClone(orderedData));

                super.handleOrderedData(dataId, orderedData);
            }

            @Override
            public void handleNewestData(short dataId, T newestData) {
                newestDatas.put(protocolId, serializationClone(newestData));

                super.handleNewestData(dataId, newestData);
            }

            @Override
            public void handleUnorderedData(short dataId, T unorderedData) {
                Queue<T> exceptionalQueue = (Queue<T>) exceptionalQueues.get(protocolId);
                if (exceptionalQueue == null) {
                    exceptionalQueue = new LinkedList<T>();
                    exceptionalQueues.put(protocolId, exceptionalQueue);
                }
                exceptionalQueue.offer(serializationClone(unorderedData));

                super.handleUnorderedData(dataId, unorderedData);
            }

            @Override
            public void handleUnackedData(short dataId, T unackedData) {
                Queue<T> exceptionalQueue = (Queue<T>) exceptionalQueues.get(protocolId);
                if (exceptionalQueue == null) {
                    exceptionalQueue = new LinkedList<T>();
                    exceptionalQueues.put(protocolId, exceptionalQueue);
                }
                exceptionalQueue.offer(serializationClone(unackedData));

                super.handleUnackedData(dataId, unackedData);
            }
        };
        Protocol<T> protocol;
        if (hostName != null)
            protocol = new Protocol<T>(protocolListener, Logger.getConsoleLogger(hostName));
        else
            protocol = new Protocol<T>(protocolListener);
        protocols.put(protocolId, protocol);

        return new ProtocolHandle<T>(protocolId, this);
    }

    @SuppressWarnings("all")
    private final List sendDatas = new ArrayList<Object>();

    @SuppressWarnings("unchecked")
    synchronized <T extends Serializable> void send(ProtocolId protocolId, T data) throws IOException {
        sendDatas.clear();
        if (data != null)
            sendDatas.add(serializationClone(data));
        send(protocolId, (List<T>) sendDatas);
    }

    @SuppressWarnings("unchecked")
    synchronized <T extends Serializable> void send(ProtocolId protocolId, List<T> datas) throws IOException {
        if (datas != sendDatas) {
            sendDatas.clear();
            for (T data : datas) {
                sendDatas.add(serializationClone(data));
            }
        }

        buffer.clear();
        buffer.put(protocolId.getTopic());
        bufferOutput.setBuffer(buffer);
        Protocol<T> protocol = (Protocol<T>) protocols.get(protocolId);
        protocol.send(sendDatas, objectOutput);
        bufferOutput.flush();

        buffer.flip();
        channel.send(buffer, protocolId.getRemoteAddress());
    }

    @SuppressWarnings("unchecked")
    <T extends Serializable> void send(ProtocolId protocolId) {
        ProtocolHandleListener<T> listener = (ProtocolHandleListener<T>) listeners.get(protocolId);

        Queue<T> exceptionalQueue = (Queue<T>) exceptionalQueues.get(protocolId);
        T unackedData = exceptionalQueue != null ? exceptionalQueue.poll() : null;
        while (unackedData != null) {
            if (listener != null)
                listener.handleExceptionalData(unackedData);

            unackedData = exceptionalQueue.poll();
        }
    }

    private final Map<ProtocolId, Object> newestDatas = new ConcurrentHashMap<ProtocolId, Object>();
    private final Map<ProtocolId, Queue<?>> receivedQueues = new ConcurrentHashMap<ProtocolId, Queue<?>>();
    private final Map<ProtocolId, Queue<?>> orderedQueues = new ConcurrentHashMap<ProtocolId, Queue<?>>();
    private final Map<ProtocolId, Queue<?>> exceptionalQueues = new ConcurrentHashMap<ProtocolId, Queue<?>>();

    @SuppressWarnings("unchecked")
    synchronized void receive() throws IOException, ClassNotFoundException {
        buffer.clear();
        SocketAddress remoteAddress = channel.receive(buffer);
        while (remoteAddress != null) {
            buffer.flip();
            ProtocolId protocolId = new ProtocolId(buffer.get(), remoteAddress);
            bufferInput.setBuffer(buffer);

            Protocol<?> protocol = protocols.get(protocolId);
            NavigableMap<Short, ?> receivedEntries = protocol.receive(objectInput);
            for (Map.Entry<Short, ?> receivedEntry : receivedEntries.entrySet()) {
                Serializable receivedData = (Serializable) receivedEntry.getValue();
                {
                    Queue<Object> receivedQueue = (Queue<Object>) receivedQueues.get(protocolId);
                    if (receivedQueue == null) {
                        receivedQueue = new LinkedList<Object>();
                        receivedQueues.put(protocolId, receivedQueue);
                    }
                    receivedQueue.offer(serializationClone(receivedData));
                }
            }

            buffer.clear();
            remoteAddress = channel.receive(buffer);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends Serializable> T receive(ProtocolId protocolId) {
        ProtocolHandleListener<T> listener = (ProtocolHandleListener<T>) listeners.get(protocolId);

        Queue<T> exceptionalQueue = (Queue<T>) exceptionalQueues.get(protocolId);
        T unorderedData = exceptionalQueue != null ? exceptionalQueue.poll() : null;
        while (unorderedData != null) {
            if (listener != null)
                listener.handleExceptionalData(unorderedData);

            unorderedData = exceptionalQueue.poll();
        }

        Queue<T> orderedQueue = (Queue<T>) orderedQueues.get(protocolId);
        T orderedData = orderedQueue != null ? orderedQueue.poll() : null;
        while (orderedData != null) {
            if (listener != null)
                listener.handleOrderedData(orderedData);

            orderedData = orderedQueue.poll();
        }

        Queue<T> receivedQueue = (Queue<T>) receivedQueues.get(protocolId);
        T receivedData = receivedQueue != null ? receivedQueue.poll() : null;
        if (receivedData == null) {
            T newestData = (T) newestDatas.remove(protocolId);
            if (newestData != null && listener != null)
                listener.handleNewestData(newestData);
        }

        return receivedData;
    }

    @SuppressWarnings("unchecked")
    private synchronized <T extends Serializable> T serializationClone(T in) {
        if (in == null) return null;

        T out;
        try {
            bufferForCloning.clear();
            bufferOutputForCloning.setBuffer(bufferForCloning);
            objectOutputForCloning.writeObject(in);
            bufferOutputForCloning.flush();

            bufferForCloning.flip();
            bufferInputForCloning.setBuffer(bufferForCloning);
            out = (T) objectInputForCloning.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Application critical fault occurred.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Application critical fault occurred.");
        }
        return out;
    }
}
