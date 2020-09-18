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
import com.github.mucaho.jnetrobust.example.ProtocolHandleListener.ProtocolException;

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
    private final Map<ProtocolId, Protocol> protocols = new ConcurrentHashMap<ProtocolId, Protocol>();
    private final Map<ProtocolId, ProtocolHandleListener<?>> listeners = new ConcurrentHashMap<ProtocolId, ProtocolHandleListener<?>>();

    // serialization fields
    private final Kryo kryo;

    private final ByteBuffer buffer = ByteBuffer.allocate(4096);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    private final ByteBuffer bufferForSerialization = ByteBuffer.allocate(4096);
    private final ByteBufferInput bufferInputForSerialization = new ByteBufferInput();
    private final ByteBufferOutput bufferOutputForSerialization = new ByteBufferOutput();
    private final KryoObjectInput objectInputForSerialization;
    private final KryoObjectOutput objectOutputForSerialization;

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

        objectInputForSerialization = new KryoObjectInput(kryo, bufferInputForSerialization);
        objectOutputForSerialization = new KryoObjectOutput(kryo, bufferOutputForSerialization);

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

        ProtocolListener protocolListener = new ProtocolListener() {
            @Override
            public void handleOrderedData(short dataId, ByteBuffer orderedData) {
                Queue<Serializable> orderedQueue = (Queue<Serializable>) orderedQueues.get(protocolId);
                if (orderedQueue == null) {
                    orderedQueue = new LinkedList<Serializable>();
                    orderedQueues.put(protocolId, orderedQueue);
                }

                int dataCount = orderedData.getInt();
                for (int i = 0; i < dataCount; ++i) {
                    T value = null;
                    try {
                        value = deserialize(orderedData);
                    } catch (IOException e) {
                        handleExceptionalData(protocolId, e);
                    } catch (ClassNotFoundException e) {
                        handleExceptionalData(protocolId, e);
                    }
                    orderedQueue.offer(value);
                }

                super.handleOrderedData(dataId, orderedData);
            }

            @Override
            public void handleNewestData(short dataId, ByteBuffer newestData) {
                int dataCount = newestData.getInt();
                for (int i = 0; i < dataCount; ++i) {
                    T value = null;
                    try {
                        value = deserialize(newestData);
                    } catch (IOException e) {
                        handleExceptionalData(protocolId, e);
                    } catch (ClassNotFoundException e) {
                        handleExceptionalData(protocolId, e);
                    }
                    newestDatas.put(protocolId, value);
                }

                super.handleNewestData(dataId, newestData);
            }

            @Override
            public void handleUnorderedData(short dataId, ByteBuffer unorderedData) {
                int dataCount = unorderedData.getInt();
                for (int i = 0; i < dataCount; ++i) {
                    T value = null;
                    try {
                        value = deserialize(unorderedData);
                    } catch (IOException e) {
                        handleExceptionalData(protocolId, e);
                    } catch (ClassNotFoundException e) {
                        handleExceptionalData(protocolId, e);
                    }
                    handleExceptionalData(protocolId, new ProtocolException(Logger.LoggingEvent.UNORDERED, value));
                }

                super.handleUnorderedData(dataId, unorderedData);
            }

            @Override
            public void handleUnackedData(short dataId, ByteBuffer unackedData) {
                int dataCount = unackedData.getInt();
                for (int i = 0; i < dataCount; ++i) {
                    T value = null;
                    try {
                        value = deserialize(unackedData);
                    } catch (IOException e) {
                        handleExceptionalData(protocolId, e);
                    } catch (ClassNotFoundException e) {
                        handleExceptionalData(protocolId, e);
                    }
                    handleExceptionalData(protocolId, new ProtocolException(Logger.LoggingEvent.NOTACKED, value));
                }

                super.handleUnackedData(dataId, unackedData);
            }
        };

        Protocol protocol;
        if (hostName != null)
            protocol = new Protocol(protocolListener, new Logger.AbstractConsoleLogger(hostName) {
                @Override
                protected String deserialize(ByteBuffer dataBuffer) {
                    StringBuilder sb = new StringBuilder();

                    int dataCount = dataBuffer.getInt();
                    for (int i = 0; i < dataCount; ++i) {
                        T value = null;
                        try {
                            value = ProtocolHost.this.deserialize(dataBuffer);
                        } catch (IOException e) {
                            handleExceptionalData(protocolId, e);
                        } catch (ClassNotFoundException e) {
                            handleExceptionalData(protocolId, e);
                        }
                        sb.append(value).append("\t");
                    }

                    return sb.toString();
                }
            });
        else
            protocol = new Protocol(protocolListener);
        protocols.put(protocolId, protocol);

        return new ProtocolHandle<T>(protocolId, this);
    }

    private void handleExceptionalData(ProtocolId protocolId, Exception exception) {
        Queue<Exception> exceptionalQueue = exceptionalQueues.get(protocolId);
        if (exceptionalQueue == null) {
            exceptionalQueue = new LinkedList<Exception>();
            exceptionalQueues.put(protocolId, exceptionalQueue);
        }
        exceptionalQueue.offer(exception);
    }

    @SuppressWarnings("all")
    private final List sendDatas = new ArrayList<Object>();
    private final ByteBuffer sendBuffer = ByteBuffer.allocateDirect(4096);

    @SuppressWarnings("unchecked")
    synchronized <T extends Serializable> void send(ProtocolId protocolId, T data) throws IOException {
        sendDatas.clear();
        sendDatas.add(data);
        send(protocolId, (List<T>) sendDatas);
    }

    synchronized <T extends Serializable> void send(ProtocolId protocolId, List<T> datas) throws IOException {
        // make sure to at least send empty packet in unidirectional communication
        if (datas == null) {
            sendDatas.clear();
            sendDatas.add(null);
            datas = sendDatas;
        }

        Protocol protocol = protocols.get(protocolId);

        int dataCount = 0;
        sendBuffer.clear();
        sendBuffer.putInt(dataCount);

        for (int i = 0, l = datas.size(); i < l; ++i) {
            ByteBuffer dataBuffer = serialize(datas.get(i));

            // if there is data to send or current sendBuffer is not full
            if (dataBuffer != null && (sendBuffer.position() + dataBuffer.limit() <= protocol.getMaximumDataSize())) {
                sendBuffer.put(dataBuffer);
                sendBuffer.putInt(0, ++dataCount);
            }
            // else send each of the packets generated out of the sendBuffer via one UDP datagram
            else {
                sendBuffer.flip();
                doSend(sendBuffer, protocolId, protocol);

                sendBuffer.clear();
                sendBuffer.putInt((dataCount = 0));
            }
        }
        if (dataCount > 0) {
            sendBuffer.flip();
            doSend(sendBuffer, protocolId, protocol);
        }

    }

    private void doSend(ByteBuffer dataBuffer, ProtocolId protocolId, Protocol protocol) throws IOException {
        NavigableMap<Short, Packet> packetMap = protocol.send(dataBuffer);
        Short key = packetMap.isEmpty() ? null : packetMap.firstKey();
        boolean currentKeyIsOkToBeNull = !packetMap.isEmpty() && packetMap.firstKey() == null;
        while (key != null || currentKeyIsOkToBeNull) {
            buffer.clear();
            buffer.put(protocolId.getTopic());
            bufferOutput.setBuffer(buffer);
            protocol.send(packetMap.get(key), objectOutput);
            bufferOutput.flush();

            buffer.flip();
            channel.send(buffer, protocolId.getRemoteAddress());

            key = packetMap.higherKey(key);
            currentKeyIsOkToBeNull = false;
        }
    }

    @SuppressWarnings("unchecked")
    <T extends Serializable> void send(ProtocolId protocolId) {
        ProtocolHandleListener<T> listener = (ProtocolHandleListener<T>) listeners.get(protocolId);

        Queue<? extends Exception> exceptionalQueue = exceptionalQueues.get(protocolId);
        Exception exception = exceptionalQueue != null ? exceptionalQueue.poll() : null;
        while (exception != null) {
            if (listener != null)
                listener.handleExceptionalData(exception);

            exception = exceptionalQueue.poll();
        }
    }

    private final Map<ProtocolId, Object> newestDatas = new ConcurrentHashMap<ProtocolId, Object>();
    private final Map<ProtocolId, Queue<?>> receivedQueues = new ConcurrentHashMap<ProtocolId, Queue<?>>();
    private final Map<ProtocolId, Queue<?>> orderedQueues = new ConcurrentHashMap<ProtocolId, Queue<?>>();
    private final Map<ProtocolId, Queue<Exception>> exceptionalQueues =
            new ConcurrentHashMap<ProtocolId, Queue<Exception>>();

    @SuppressWarnings("unchecked")
    synchronized void receive() throws IOException, ClassNotFoundException {
        buffer.clear();
        SocketAddress remoteAddress = channel.receive(buffer);
        while (remoteAddress != null) {
            buffer.flip();

            ProtocolId protocolId = new ProtocolId(buffer.get(), remoteAddress);
            Protocol protocol = protocols.get(protocolId);

            bufferInput.setBuffer(buffer);
            NavigableMap<Short, ByteBuffer> receivedEntries = protocol.receive(objectInput);
            Short key = receivedEntries.isEmpty() ? null : receivedEntries.firstKey();
            while (key != null) {
                ByteBuffer receivedData = receivedEntries.get(key);
                Queue<Object> receivedQueue = (Queue<Object>) receivedQueues.get(protocolId);
                if (receivedQueue == null) {
                    receivedQueue = new LinkedList<Object>();
                    receivedQueues.put(protocolId, receivedQueue);
                }
                int dataCount = receivedData.getInt();
                for (int i = 0; i < dataCount; ++i)
                    receivedQueue.offer(deserialize(receivedData));

                key = receivedEntries.higherKey(key);
            }

            buffer.clear();
            remoteAddress = channel.receive(buffer);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends Serializable> T receive(ProtocolId protocolId) {
        ProtocolHandleListener<T> listener = (ProtocolHandleListener<T>) listeners.get(protocolId);

        Queue<? extends Exception> exceptionalQueue = exceptionalQueues.get(protocolId);
        Exception exception = exceptionalQueue != null ? exceptionalQueue.poll() : null;
        while (exception != null) {
            if (listener != null)
                listener.handleExceptionalData(exception);

            exception = exceptionalQueue.poll();
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

    private synchronized <T extends Serializable> ByteBuffer serialize(T in) throws IOException {
        if (in == null) return null;

        bufferForSerialization.clear();
        bufferOutputForSerialization.setBuffer(bufferForSerialization);
        objectOutputForSerialization.writeObject(in);
        bufferOutputForSerialization.flush();

        bufferForSerialization.flip();
        return bufferForSerialization;
    }

    @SuppressWarnings("unchecked")
    private synchronized <T extends Serializable> T deserialize(ByteBuffer in) throws IOException, ClassNotFoundException {
        if (in == null) return null;

        bufferForSerialization.clear();
        in.mark();
        bufferForSerialization.put(in);
        bufferForSerialization.flip();
        bufferInputForSerialization.setBuffer(bufferForSerialization);
        T out = (T) objectInputForSerialization.readObject();
        in.reset();
        in.position(in.position() + bufferForSerialization.position());
        return out;
    }
}
