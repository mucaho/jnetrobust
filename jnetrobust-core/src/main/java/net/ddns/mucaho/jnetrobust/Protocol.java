package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.util.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public class Protocol<T> implements Comparator<Short> {
    private final RetransmissionController<T> controller;
    private final boolean shouldRetransmit;

    public Protocol(ProtocolListener<T> protocolListener) {
        this(protocolListener, null);
    }
    public Protocol(ProtocolConfig<T> config) {
        this(config, null);
    }
    public Protocol(ProtocolListener<T> protocolListener, Logger logger) {
        this(new ProtocolConfig<T>(protocolListener), logger);
    }
    public Protocol(ProtocolConfig<T> config, Logger logger) {
        this.shouldRetransmit = config.shouldRetransmit();
        if (logger != null) {
            ProtocolListener<T> debugListener = new DebugProtocolListener<T>(config.listener, logger);
            this.controller = new DebugController<T>(new ProtocolConfig<T>(debugListener, config), logger);
        } else {
            this.controller = new RetransmissionController<T>(config);
        }

    }




    private final PacketEntry<T> sentPacketOut = new PacketEntry<T>();

    public synchronized Map.Entry<Short, Packet<T>> send() {
        return send(null);
    }

    public synchronized Map.Entry<Short, Packet<T>> send(T data) {
        Packet<T> packet = controller.produce();
        if (shouldRetransmit) {
            Collection<Metadata<T>> retransmits = controller.retransmit();
            for (Metadata<T> retransmit : retransmits) {
                controller.send(packet, retransmit);
            }
        }
        if (data != null)
            controller.send(packet, controller.produce(data));

        sentPacketOut.packet = packet;
        sentPacketOut.id = data != null ? packet.getLastMetadata().getDataId() : null;
        return sentPacketOut;
    }

    public synchronized Map.Entry<Short, Packet<T>> send(T data, ObjectOutput objectOutput) throws IOException {
        Map.Entry<Short, Packet<T>> packetEntry = send(data);
        Packet.<T>writeExternalStatic(packetEntry.getValue(), objectOutput);
        return packetEntry;
    }



    private final NavigableMap<Short, T> receivedDatas = new TreeMap<Short, T>(IdComparator.instance);
    private final NavigableMap<Short, T> receivedDatasOut = CollectionUtils.unmodifiableNavigableMap(receivedDatas);

    public synchronized NavigableMap<Short, T> receive(Packet<T> packet) {
        receivedDatas.clear();

        controller.consume(packet);
        Metadata<T> metadata = controller.receive(packet);
        while (metadata != null) {
            receivedDatas.put(metadata.getDataId(), controller.consume(metadata));
            metadata = controller.receive(packet);
        }

        return receivedDatasOut;
    }


    public synchronized NavigableMap<Short, T> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Packet<T> packet = Packet.<T>readExternalStatic(objectInput);
        return receive(packet);
    }




    @Override
    public int compare(Short id1, Short id2) {
        return IdComparator.instance.compare(id1, id2);
    }

    public long getSmoothedRTT() {
        return controller.getSmoothedRTT();
    }

    public long getRTTVariation() {
        return controller.getRTTVariation();
    }

    private static class PacketEntry<T> implements Map.Entry<Short, Packet<T>> {
        private Short id;
        private Packet<T> packet;

        public PacketEntry() {
        }


        @Override
        public Short getKey() {
            return id;
        }

        @Override
        public Packet<T> getValue() {
            return packet;
        }

        @Override
        public Packet<T> setValue(Packet<T> packet) {
            throw new UnsupportedOperationException();
        }
    }
}
