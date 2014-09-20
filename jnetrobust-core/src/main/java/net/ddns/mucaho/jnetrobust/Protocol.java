package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.Logger;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;
import net.ddns.mucaho.jnetrobust.util.UDPListener;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.ProtocolException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


public class Protocol implements Comparator<Short> {
    public final static int MAX_PACKETS_SENT = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;

    private final UDPListener udpListener;
    private final RetransmissionController controller;

    public Protocol(UDPListener udpListener) {
        this(new Config(udpListener));
    }
    public Protocol(Config config) {
        this(config, null, null);
    }
    public Protocol(UDPListener udpListener, String name, Logger logger) {
        this(new Config(udpListener), name, logger);
    }
    public Protocol(Config config, String name, Logger logger) {
        this.udpListener = config.listener;
        if (name != null && logger != null)
            this.controller = new DebugController(config, name, logger);
        else
            this.controller = new RetransmissionController(config);

    }

    Collection<IdPacket> packets = new LinkedList<IdPacket>();
    Collection<IdPacket> packetsOut = Collections.unmodifiableCollection(packets);
    public synchronized Collection<IdPacket> send(Object data) {
        packets.clear();

        Collection<? extends MultiKeyValue> retransmits = controller.retransmit();
        for (MultiKeyValue retransmit : retransmits) {
            packets.add(controller.send(retransmit));
        }
        packets.add(controller.send(data));

        return packetsOut;
    }

    public synchronized IdPacket send(Object data, ObjectOutput objectOutput) throws IOException {
        Collection<IdPacket> sendPackets = send(data);
        if (sendPackets.size() > MAX_PACKETS_SENT)
            throw new ProtocolException();

        objectOutput.writeByte(sendPackets.size());
        IdPacket lastPacket = null;
        for (IdPacket sendPacket: sendPackets) {
            Packet.writeExternalStatic(sendPacket.getPacket(), objectOutput);
            lastPacket = sendPacket;
        }

        return lastPacket;
    }


    public synchronized IdData receive(Packet pkg) {
       return controller.receive(pkg);
    }

    Collection<IdData> datas = new LinkedList<IdData>();
    Collection<IdData> datasOut = Collections.unmodifiableCollection(datas);
    public synchronized Collection<IdData> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        datas.clear();

        int packetCount = objectInput.readUnsignedByte();
        for (int i = 0; i < packetCount; ++packetCount) {
            Packet packet = Packet.readExternalStatic(objectInput);
            datas.add(receive(packet));
        }

        return datas;
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
}
