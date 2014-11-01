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

public class DefaultHost<T> {
    public static interface DataListener<T> {
        public void handleOrderedData(T orderedData);
        public void handleNewestData(T newestData);
        //TODO add exceptional callback
    }


    public static class HostHandle<T> {
        private final Byte topic;
        private final SocketAddress targetAddress;
        private final DefaultHost<T> host;
        private HostHandle(Byte topic, SocketAddress targetAddress, DefaultHost<T> host) {
            this.host = host;
            this.targetAddress = targetAddress;
            this.topic = topic;
        }

        public void send(T data) throws IOException {
            host.send(topic, targetAddress, data);
        }
        public T receive() throws IOException, ClassNotFoundException {
            host.receive();
            return host.receive(topic);
        }
    }

    private final String hostName;

    // protocol fields
    private final Map<Byte, Protocol<T>> protocols = new HashMap<Byte, Protocol<T>>();
    private final Map<Byte, DataListener<T>> listeners = new HashMap<Byte, DataListener<T>>();

    // serialization fields
    private final Kryo kryo;
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    // network communication fields
    private final DatagramChannel channel;

    public DefaultHost(String hostName, SocketAddress hostAddress, Class<T> dataClass) throws IOException {
        // setup network communication
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(hostAddress);

        // setup serialization
        kryo = new Kryo();
        kryo.register(Packet.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(Metadata.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(dataClass);
        objectInput = new KryoObjectInput(kryo, bufferInput);
        objectOutput = new KryoObjectOutput(kryo, bufferOutput);

        this.hostName = hostName;
    }



    public HostHandle<T> register(byte topic, SocketAddress targetAddress) {
        return register(topic, targetAddress, null);
    }
    public HostHandle<T> register(byte topic, SocketAddress targetAddress, final DataListener<T> listener) {
        ProtocolListener<T> protocolListener = new ProtocolListener<T>() {
            @Override
            public void handleOrderedData(short dataId, T orderedData) {
                if (listener != null)
                    listener.handleOrderedData(orderedData);
            }
        };
        listeners.put(topic, listener);

        Protocol<T> protocol;
        if (hostName != null)
            protocol = new Protocol<T>(protocolListener, Logger.getConsoleLogger(hostName));
        else
            protocol = new Protocol<T>(protocolListener);
        protocols.put(topic, protocol);

        return new HostHandle<T>(topic, targetAddress, this);
    }




    private void send(byte topic, SocketAddress targetAddress, T data) throws IOException {
        buffer.clear();
        buffer.put(topic);
        bufferOutput.setBuffer(buffer);
        protocols.get(topic).send(data, objectOutput);

        buffer.flip();
        channel.send(buffer, targetAddress);
    }

    private Map<Byte, Short> newestIds = new HashMap<Byte, Short>();
    private Map<Byte, T> newestDatas = new HashMap<Byte, T>();
    private Map<Byte, Queue<T>> receivedQueues = new HashMap<Byte, Queue<T>>();

    private void receive() throws IOException, ClassNotFoundException {
        buffer.clear();
        SocketAddress senderAddress = channel.receive(buffer);
        while (senderAddress != null) {
            buffer.flip();
            byte topic = buffer.get();
            bufferInput.setBuffer(buffer);

            Protocol<T> protocol = protocols.get(topic);
            NavigableMap<Short, T> receivedEntries = protocol.receive(objectInput);
            for (Map.Entry<Short, T> receivedEntry: receivedEntries.entrySet()) {
                Short receivedId = receivedEntry.getKey();
                T receivedData = receivedEntry.getValue();

                {
                    Queue<T> receivedQueue = receivedQueues.get(topic);
                    if (receivedQueue == null) {
                        receivedQueue = new LinkedList<T>();
                        receivedQueues.put(topic, receivedQueue);
                    }
                    receivedQueue.offer(receivedData);
                }
                {
                    Short newestId = newestIds.get(topic);
                    if (newestId == null || protocol.compare(receivedId, newestId) > 0) {
                        newestIds.put(topic, receivedId);
                        newestDatas.put(topic, receivedData);
                    }
                }
            }


            buffer.clear();
            senderAddress = channel.receive(buffer);
        }
    }

    private T receive(Byte topic) {
        Queue<T> receivedQueue = receivedQueues.get(topic);
        T receivedData = receivedQueue != null ? receivedQueue.poll() : null;

        if (receivedData == null) {
            T newestData = newestDatas.remove(topic);
            if (newestData != null) {
                DataListener<T> listener = listeners.get(topic);
                if (listener != null)
                    listener.handleNewestData(newestData);
            }
        }

        return receivedData;
    }

}
