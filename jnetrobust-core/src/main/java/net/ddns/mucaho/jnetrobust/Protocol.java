package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.control.MetadataUnit;
import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.ProtocolUnit;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.util.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public class Protocol implements Comparator<Short> {
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
            ProtocolListener debugListener = new DebugProtocolListener(config.listener, name, logger);
            this.controller = new DebugController(new ProtocolConfig(debugListener, config), name, logger);
        } else {
            this.controller = new RetransmissionController(config);
        }

    }




    private final ProtocolUnitEntry sentPacketOut = new ProtocolUnitEntry();

    public synchronized Map.Entry<Short, ProtocolUnit> send(Object data) {
        ProtocolUnit packet = controller.produce();
        Collection<? extends MetadataUnit> retransmits = controller.retransmit();
        for (MetadataUnit retransmit : retransmits) {
            controller.send(packet, retransmit);
        }
        controller.send(packet, controller.produce(data));

        sentPacketOut.packet = packet;
        sentPacketOut.id = packet.getLastMetadata().getStaticReference();
        return sentPacketOut;
    }

    public synchronized Map.Entry<Short, ProtocolUnit> send(Object data, ObjectOutput objectOutput) throws IOException {
        Map.Entry<Short, ProtocolUnit> packetEntry = send(data);
        ProtocolUnit.writeExternalStatic(packetEntry.getValue(), objectOutput);
        return packetEntry;
    }



    private final NavigableMap<Short, Object> receivedDatas = new TreeMap<Short, Object>(SequenceComparator.instance);
    private final NavigableMap<Short, Object> receivedDatasOut = CollectionUtils.unmodifiableNavigableMap(receivedDatas);

    public synchronized NavigableMap<Short, Object> receive(ProtocolUnit protocolUnit) {
        receivedDatas.clear();

        controller.consume(protocolUnit);
        MetadataUnit metadata = controller.receive(protocolUnit);
        while (metadata != null) {
            receivedDatas.put(metadata.getStaticReference(), controller.consume(metadata));
            metadata = controller.receive(protocolUnit);
        }

        return receivedDatasOut;
    }


    public synchronized NavigableMap<Short, Object> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        ProtocolUnit packet = ProtocolUnit.readExternalStatic(objectInput);
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

    private static class ProtocolUnitEntry implements Map.Entry<Short, ProtocolUnit> {
        private Short id;
        private ProtocolUnit packet;

        public ProtocolUnitEntry() {
        }


        @Override
        public Short getKey() {
            return id;
        }

        @Override
        public ProtocolUnit getValue() {
            return packet;
        }

        @Override
        public ProtocolUnit setValue(ProtocolUnit protocolUnit) {
            throw new UnsupportedOperationException();
        }
    }
}
