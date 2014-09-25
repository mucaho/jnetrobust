package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoObjectInput;
import com.esotericsoftware.kryo.io.KryoObjectOutput;
import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.Protocol;
import net.ddns.mucaho.jnetrobust.ProtocolListener;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.controller.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.Queue;

public class DefaultHost<T> {
    public static interface DataListener<T> {
        public void handleOrderedData(T orderedData);
        public void handleNewestData(T newestData);
        //TODO add exceptional callback
    }

    // protocol fields
    private final Protocol<T> protocol;
    private final DataListener<T> listener;

    // serialization fields
    private final Kryo kryo;
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    // network communication fields
    private final DatagramChannel channel;
    private final InetSocketAddress targetAddress;

    public DefaultHost(String hostName, InetSocketAddress hostAddress, final InetSocketAddress targetAddress,
                       Class<T> dataClass, final DataListener<T> dataListener) throws IOException {
        this.listener = dataListener;

        // setup network communication
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(hostAddress);
        this.targetAddress = targetAddress;

        // setup serialization
        kryo = new Kryo();
        kryo.register(Packet.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(Metadata.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(dataClass);
        objectInput = new KryoObjectInput(kryo, bufferInput);
        objectOutput = new KryoObjectOutput(kryo, bufferOutput);

        // setup virtual protocol
        ProtocolListener<T> protocolListener = new ProtocolListener<T>() {
            @Override
            public void handleOrderedData(short dataId, T orderedData) {
                dataListener.handleOrderedData(orderedData);
            }
        };
        if (hostName != null)
            this.protocol = new Protocol<T>(protocolListener, Logger.getConsoleLogger(hostName));
        else
            this.protocol = new Protocol<T>(protocolListener);
    }

    public void send() throws IOException {
        send(null);
    }

    public void send(T data) throws IOException {
        buffer.clear();
        bufferOutput.setBuffer(buffer);
        protocol.send(data, objectOutput);

        buffer.flip();
        channel.send(buffer, targetAddress);
    }

    private Queue<T> outQueue = new LinkedList<T>();
    private Short newestId = null;
    private T newestData = null;

    public Queue<T> receive() throws IOException, ClassNotFoundException {
        outQueue.clear();

        buffer.clear();
        SocketAddress senderAddress = channel.receive(buffer);
        while (senderAddress != null) {
            buffer.flip();
            bufferInput.setBuffer(buffer);

            NavigableMap<Short, T> receivedDatas = protocol.receive(objectInput);
            for (T receivedData: receivedDatas.values())
                outQueue.add(receivedData);
            if (!receivedDatas.isEmpty() &&
                    (newestId == null || protocol.compare(receivedDatas.lastKey(), newestId) > 0)) {
                newestId = receivedDatas.lastKey();
                newestData = receivedDatas.get(newestId);
            }

            buffer.clear();
            senderAddress = channel.receive(buffer);
        }

        if (newestData != null) {
            listener.handleNewestData(newestData);
            newestData = null;
        }

        return outQueue;
    }
}
