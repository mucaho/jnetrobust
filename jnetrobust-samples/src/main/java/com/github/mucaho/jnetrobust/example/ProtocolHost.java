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
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.controller.Packet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;

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
    public interface DataListener<T> {
        void handleOrderedData(T orderedData);
        void handleNewestData(T newestData);
        //TODO add exceptional callback
    }

    public static class ProtocolHandle<T> {
        private final ProtocolId protocolId;
        private final ProtocolHost host;
        private ProtocolHandle(ProtocolId protocolId, ProtocolHost host) {
            this.host = host;
            this.protocolId = protocolId;
        }

        private List<T> sendDatas = new ArrayList<T>();

        public void send() throws IOException {
            sendDatas.clear();
            host.send(protocolId, sendDatas);
        }
        public void send(T data) throws IOException {
            sendDatas.clear();
            sendDatas.add(data);

            host.send(protocolId, sendDatas);
        }
        public void send(List<T> datas) throws IOException {
            host.send(protocolId, datas);
        }

        public T receive() throws IOException, ClassNotFoundException {
            host.receive();
            return host.receive(protocolId);
        }
    }

    private final String hostName;

    // protocol fields
    private final Map<ProtocolId, Protocol<?>> protocols = new HashMap<ProtocolId, Protocol<?>>();
    private final Map<ProtocolId, DataListener<?>> listeners = new HashMap<ProtocolId, DataListener<?>>();

    // serialization fields
    private final Kryo kryo;
    private final ByteBuffer buffer = ByteBuffer.allocate(4096);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    // network communication fields
    private final DatagramChannel channel;

    public ProtocolHost(String hostName, SocketAddress localAddress, Class<?>... dataClasses) throws IOException {
        // setup network communication
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(localAddress);

        // setup serialization
        kryo = new Kryo();
        kryo.register(Packet.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(Metadata.class); // add argument `new ExternalizableSerializer()` if needed
        for (Class<?> dataClass : dataClasses)
            kryo.register(dataClass);
        objectInput = new KryoObjectInput(kryo, bufferInput);
        objectOutput = new KryoObjectOutput(kryo, bufferOutput);

        this.hostName = hostName;
    }

    public <T> ProtocolHandle<T> register(byte topic, SocketAddress remoteAddress) {
        return register(topic, remoteAddress, null);
    }

    @SuppressWarnings("unchecked")
    public <T> ProtocolHandle<T> register(byte topic, SocketAddress remoteAddress, final DataListener<T> listener) {
        final ProtocolId protocolId = new ProtocolId(topic, remoteAddress);

        listeners.put(protocolId, listener);

        ProtocolListener<T> protocolListener = new ProtocolListener<T>() {
            @Override
            public void handleOrderedData(short dataId, T orderedData) {
                Queue<T> orderedQueue = (Queue<T>) orderedQueues.get(protocolId);
                if (orderedQueue == null) {
                    orderedQueue = new LinkedList<T>();
                    orderedQueues.put(protocolId, orderedQueue);
                }
                orderedQueue.offer(orderedData);

                super.handleOrderedData(dataId, orderedData);
            }

            @Override
            public void handleNewestData(short dataId, T newestData) {
                newestDatas.put(protocolId, newestData);

                super.handleNewestData(dataId, newestData);
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

    @SuppressWarnings("unchecked")
    private <T> void send(ProtocolId protocolId, List<T> datas) throws IOException {
        buffer.clear();
        buffer.put(protocolId.topic);
        bufferOutput.setBuffer(buffer);
        Protocol<T> protocol = (Protocol<T>) protocols.get(protocolId);
        protocol.send(datas, objectOutput);
        bufferOutput.flush();

        buffer.flip();
        channel.send(buffer, protocolId.remoteAddress);
    }

    private final Map<ProtocolId, Object> newestDatas = new HashMap<ProtocolId, Object>();
    private final Map<ProtocolId, Queue<?>> receivedQueues = new HashMap<ProtocolId, Queue<?>>();
    private final Map<ProtocolId, Queue<?>> orderedQueues = new HashMap<ProtocolId, Queue<?>>();

    @SuppressWarnings("unchecked")
    private void receive() throws IOException, ClassNotFoundException {
        buffer.clear();
        SocketAddress remoteAddress = channel.receive(buffer);
        while (remoteAddress != null) {
            buffer.flip();
            ProtocolId protocolId = new ProtocolId(buffer.get(), remoteAddress);
            bufferInput.setBuffer(buffer);

            Protocol<?> protocol = protocols.get(protocolId);
            NavigableMap<Short, ?> receivedEntries = protocol.receive(objectInput);
            for (Map.Entry<Short, ?> receivedEntry : receivedEntries.entrySet()) {
                Object receivedData = receivedEntry.getValue();
                {
                    Queue<Object> receivedQueue = (Queue<Object>) receivedQueues.get(protocolId);
                    if (receivedQueue == null) {
                        receivedQueue = new LinkedList<Object>();
                        receivedQueues.put(protocolId, receivedQueue);
                    }
                    receivedQueue.offer(receivedData);
                }
            }

            buffer.clear();
            remoteAddress = channel.receive(buffer);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T receive(ProtocolId protocolId) {
        DataListener<T> listener = (DataListener<T>) listeners.get(protocolId);

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

    private static class ProtocolId {
        private final byte topic;
        private final SocketAddress remoteAddress;

        private ProtocolId(byte topic, SocketAddress remoteAddress) {
            this.topic = topic;
            this.remoteAddress = remoteAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProtocolId that = (ProtocolId) o;

            if (topic != that.topic) return false;
            if (!remoteAddress.equals(that.remoteAddress)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) topic;
            result = 31 * result + remoteAddress.hashCode();
            return result;
        }
    }
}
