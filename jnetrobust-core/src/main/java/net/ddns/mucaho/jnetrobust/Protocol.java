package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.util.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public class Protocol implements Comparator<Short> {
    private final ProtocolListener protocolListener;
    private final RetransmissionController controller;

    public Protocol(ProtocolListener protocolListener) {
        this(new ProtocolConfig(protocolListener));
    }
    public Protocol(ProtocolConfig config) {
        this(config, null, null);
    }
    public Protocol(ProtocolListener protocolListener, String name, Logger logger) {
        this(new ProtocolConfig(protocolListener), name, logger);
    }
    public Protocol(ProtocolConfig config, String name, Logger logger) {
        if (name != null && logger != null) {
            this.controller = new DebugController(config, name, logger) {
                @Override
                public Object doReceive(MultiKeyValue multiKeyValue) {
                    receivedDatas.put(multiKeyValue.getStaticReference(), multiKeyValue.getValue());
                    return super.receive(multiKeyValue);
                }
            };
            this.protocolListener = new DebugProtocolListener(config.listener, name, logger);
        } else {
            this.controller = new RetransmissionController(config) {
                @Override
                public Object doReceive(MultiKeyValue multiKeyValue) {
                    receivedDatas.put(multiKeyValue.getStaticReference(), multiKeyValue.getValue());
                    return super.receive(multiKeyValue);
                }
            };
            this.protocolListener = config.listener;
        }

    }




    private final PacketEntry sentPacketOut = new PacketEntry();

    public synchronized Map.Entry<Short, Packet> send(Object data) {
        Collection<? extends MultiKeyValue> retransmits = controller.retransmit();
        Packet packet = null;
        for (MultiKeyValue retransmit : retransmits) {
            packet = controller.send(retransmit, packet);
        }
        packet = controller.send(data, packet);

        sentPacketOut.packet = packet;
        sentPacketOut.id = packet.getLastData().getStaticReference();
        return sentPacketOut;
    }

    public synchronized Map.Entry<Short, Packet> send(Object data, ObjectOutput objectOutput) throws IOException {
        Map.Entry<Short, Packet> packetEntry = send(data);
        Packet.writeExternalStatic(packetEntry.getValue(), objectOutput);
        return packetEntry;
    }





    private final NavigableMap<Short, Object> receivedDatas = new TreeMap<Short, Object>(SequenceComparator.instance);
    private final NavigableMap<Short, Object> receivedDatasOut = CollectionUtils.unmodifiableNavigableMap(receivedDatas);

    public synchronized NavigableMap<Short, Object> receive(Packet packet) {
        receivedDatas.clear();

        Object data = controller.receive(packet, true);
        while (data != null) {
            data = controller.receive(packet, false);
        }

        return receivedDatasOut;
    }


    public synchronized NavigableMap<Short, Object> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Packet packet = Packet.readExternalStatic(objectInput);
        return receive(packet);
    }




    @Override
    public int compare(Short o1, Short o2) {
        return SequenceComparator.instance.compare(o1, o2);
    }

    public long getSmoothedRTT() {
        return controller.getSmoothedRTT();
    }

    public long getRTTVariation() {
        return controller.getRTTVariation();
    }

    private static class PacketEntry implements Map.Entry<Short, Packet> {
        private Short id;
        private Packet packet;

        public PacketEntry() {
        }


        @Override
        public Short getKey() {
            return id;
        }

        @Override
        public Packet getValue() {
            return packet;
        }

        @Override
        public Packet setValue(Packet value) {
            throw new UnsupportedOperationException();
        }
    }
}
