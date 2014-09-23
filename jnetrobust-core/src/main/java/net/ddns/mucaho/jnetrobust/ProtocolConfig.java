package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.controller.ProtocolUnit;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

public class ProtocolConfig {
    public final static int MAX_PACKET_QUEUE_LIMIT = ProtocolUnit.MAX_DATAS_PER_PACKET;
    public final static int MAX_PACKET_OFFSET_LIMIT = SequenceComparator.MAX_SEQUENCE / 4;

    private int packetQueueLimit = MAX_PACKET_QUEUE_LIMIT;
    private int packetOffsetLimit = MAX_PACKET_OFFSET_LIMIT;
    private long packetQueueTimeout = -1L;
    private int packetRetransmitLimit = -1; //FIXME set to ProtocolUnit.MAX_DATAS_PER_PACKET;
    private int K = 2;
    private int G = 25;

    public final ProtocolListener listener;

    public ProtocolConfig(ProtocolListener listener) {
        super();
        this.listener = listener;
    }


    public ProtocolConfig(ProtocolListener listener, ProtocolConfig config) {
        super();
        this.listener = listener;
        this.packetQueueLimit = config.packetQueueLimit;
        this.packetQueueTimeout = config.packetQueueTimeout;
        this.G = config.G;
        this.K = config.K;
    }


    public int getPacketQueueLimit() {
        return packetQueueLimit;
    }

    public void setPacketQueueLimit(int packetQueueLimit) {
        this.packetQueueLimit = packetQueueLimit < MAX_PACKET_QUEUE_LIMIT ?
                packetQueueLimit : MAX_PACKET_QUEUE_LIMIT;
    }

    public int getPacketOffsetLimit() {
        return packetOffsetLimit;
    }

    public void setPacketOffsetLimit(int packetOffsetLimit) {
        this.packetOffsetLimit = packetOffsetLimit < MAX_PACKET_OFFSET_LIMIT ?
                packetOffsetLimit : MAX_PACKET_OFFSET_LIMIT;
    }

    public long getPacketQueueTimeout() {
        return packetQueueTimeout;
    }

    public void setPacketQueueTimeout(long packetQueueTimeout) {
        this.packetQueueTimeout = packetQueueTimeout;
    }

    public int getPacketRetransmitLimit() {
        return packetRetransmitLimit;
    }

    public void setPacketRetransmitLimit(int packetRetransmitLimit) {
        this.packetRetransmitLimit = packetRetransmitLimit < MAX_PACKET_QUEUE_LIMIT ?
                packetRetransmitLimit : MAX_PACKET_QUEUE_LIMIT;
    }

    public int getK() {
        return K;
    }

    public int getG() {
        return G;
    }
}
