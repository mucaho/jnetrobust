package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoObjectInput;
import com.esotericsoftware.kryo.io.KryoObjectOutput;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.DebugUDPListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class DefaultHost<T> {
    public static interface OrderedDataListener<T> {
        public void handleOrderedData(T orderedData);
        //TODO add handleExceptionalState();
    }

    private final String hostName;

    // protocol fields
    private final RetransmissionController protocol;
    private final Queue<MultiKeyValue> retransmitQueue = new LinkedList<MultiKeyValue>();

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
        //TODO put in overridable methods
        this.hostName = hostName;

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
        this.protocol = new RetransmissionController(new Config(new DebugUDPListener(hostName) {
            @Override
            @SuppressWarnings("unchecked")
            public void handleOrderedTransmission(Object iterPkg) {
                super.handleOrderedTransmission(iterPkg);
                if (iterPkg != null)
                    orderedDataListener.handleOrderedData((T) iterPkg);
            }

            @Override
            public void handleTransmissionRequest() {
                super.handleTransmissionRequest();
                try {
                    //FIXME
                    internalSend((T)null);
                } catch (IOException e) {}
            }

            @Override
            public void handleTransmissionRequests(Collection<? extends MultiKeyValue> retransmitDatas) {
                super.handleTransmissionRequests(retransmitDatas);
                retransmitQueue.addAll(retransmitDatas);
            }
        }));
    }

    public void send(T data) throws IOException {
        protocol.retransmit();
        for (MultiKeyValue retransmit: retransmitQueue) {
            internalSend(protocol.send(retransmit));
            System.out.println("[" + hostName + "-RETRANSMIT]: " + retransmit.getValue().toString());
        }
        retransmitQueue.clear();

        internalSend(data);
    }

    private void internalSend(T data) throws IOException {
        internalSend(protocol.send(data));
        System.out.println("["+hostName+"-SEND]: "+data);
    }

    private void internalSend(final Packet outPacket) throws IOException {
        buffer.clear();
        bufferOutput.setBuffer(buffer);
        outPacket.writeExternal(objectOutput);

        buffer.flip();
        channel.send(buffer, targetAddress);
    }

    private Queue<T> outQueue = new LinkedList<T>();
    @SuppressWarnings("unchecked")
    public Queue<T> receive() throws IOException, ClassNotFoundException {
        outQueue.clear();

        buffer.clear();
        SocketAddress senderAddress = channel.receive(buffer);
        while(senderAddress != null) {
            buffer.flip();
            bufferInput.setBuffer(buffer);
            Packet inPacket = new Packet();
            inPacket.readExternal(objectInput);

            T data = (T)protocol.receive(inPacket);
            System.out.println("["+hostName+"-RECV]: " + data);
            if (data != null)
                outQueue.add(data);

            buffer.clear();
            senderAddress = channel.receive(buffer);
        }

        return outQueue;
    }
}
