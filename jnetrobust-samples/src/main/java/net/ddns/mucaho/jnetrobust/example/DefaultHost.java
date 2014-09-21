package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoObjectInput;
import com.esotericsoftware.kryo.io.KryoObjectOutput;
import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.Protocol;
import net.ddns.mucaho.jnetrobust.ProtocolListener;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
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
    public static interface OrderedDataListener<T> {
        public void handleOrderedData(T orderedData);
        public void handleNewestData(T newestData);
        //TODO add exceptional callback
    }

    // protocol fields
    private final Protocol protocol;
    private final OrderedDataListener<T> listener;

    // serialization fields
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private final ByteBufferInput bufferInput = new ByteBufferInput();
    private final ByteBufferOutput bufferOutput = new ByteBufferOutput();
    private final KryoObjectInput objectInput;
    private final KryoObjectOutput objectOutput;

    // network communication fields
    private final DatagramChannel channel;
    private final InetSocketAddress targetAddress;

    public DefaultHost(String hostName, Kryo kryo,
                       InetSocketAddress hostAddress, final InetSocketAddress targetAddress,
                       final OrderedDataListener<T> orderedDataListener) throws IOException {
        this.listener = orderedDataListener;

        // setup network communication
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(hostAddress);
        this.targetAddress = targetAddress;

        // setup serialization
        kryo.register(Packet.class); // add argument `new ExternalizableSerializer()` if needed
        kryo.register(MultiKeyValue.class); // add argument `new ExternalizableSerializer()` if needed
        objectInput = new KryoObjectInput(kryo, bufferInput);
        objectOutput = new KryoObjectOutput(kryo, bufferOutput);

        // setup virtual protocol
        this.protocol = new Protocol(new ProtocolListener() {
            @Override
            public void handleOrderedTransmission(short dataId, Object orderedData) {
               orderedDataListener.handleOrderedData((T) orderedData);
            }
        }, hostName, Logger.getConsoleLogger());
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
    @SuppressWarnings("unchecked")
    public Queue<T> receive() throws IOException, ClassNotFoundException {
        outQueue.clear();

        buffer.clear();
        SocketAddress senderAddress = channel.receive(buffer);
        while (senderAddress != null) {
            buffer.flip();
            bufferInput.setBuffer(buffer);
            NavigableMap<Short, Object> receivedDatas = protocol.receive(objectInput);

            if (newestId == null || protocol.compare(receivedDatas.lastKey(), newestId) > 0) {
                newestId = receivedDatas.lastKey();
                listener.handleNewestData((T) receivedDatas.get(newestId));
            }


            for (Object receivedData: receivedDatas.values())
                outQueue.add((T) receivedData);

            buffer.clear();
            senderAddress = channel.receive(buffer);
        }

        return outQueue;
    }
}
