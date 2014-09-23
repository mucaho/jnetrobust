package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.*;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

public class Controller<T> {
    protected short dataId = Short.MIN_VALUE;
    protected ReceivedMapControl<T> receivedMapHandler;

    protected short localSeq = Short.MIN_VALUE;
    protected PendingMapControl<T> pendingMapHandler;

    protected short remoteSeq = Short.MIN_VALUE;
    protected ReceivedBitsControl receivedBitsHandler;

    protected RTTControl rttHandler;


    public Controller(ProtocolConfig<T> config) {
        pendingMapHandler = new PendingMapControl<T>(config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
                if (ackedMetadata != null && directlyAcked)
                    rttHandler.updateRTT(ackedMetadata.getTime()); // update RTT

                super.notifyAcked(ackedMetadata, directlyAcked);
            }
        };

        receivedBitsHandler = new ReceivedBitsControl(SequenceComparator.instance);
        receivedMapHandler = new ReceivedMapControl<T>(dataId, config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

        rttHandler = new RTTControl(config.getK(), config.getG());
    }



    public Packet<T> produce() {
        Packet<T> packet = new Packet<T>();
        // adapt remote seq
        packet.setAck(remoteSeq);
        // Handle remote sequences
        packet.setLastAcks((int) receivedBitsHandler.getReceivedRemoteBits());

        return packet;
    }

    public Metadata<T> produce(T data) {
        // Handle data id: adapt unique data id
        return new Metadata<T>(++dataId, data);
    }

    public void send(Packet<T> packet, Metadata<T> metadata) {
        // adapt local seq
        localSeq++;

        // Handle local sequences
        pendingMapHandler.addToPending(localSeq, metadata);


        packet.addLastMetadata(metadata);
    }



    public void consume(Packet<T> packet) {
        // Handle local sequences
        pendingMapHandler.removeFromPending(packet.getAck(), packet.getLastAcks());
    }

    public Metadata<T> receive(Packet<T> packet) {
        Metadata<T> metadata = packet.removeFirstMetadata();
        if (metadata != null)
            receive(metadata);

        return metadata;
    }

    private void receive(Metadata<T> metadata) {
        short newRemoteSeq = metadata.getLastDynamicReference();

        // Handle remote sequences
        receivedBitsHandler.addToReceived(metadata.getDynamicReferences(), remoteSeq);
        // Handle metadata id
        receivedMapHandler.addToReceived(metadata);

        // adapt remote seq
        if (SequenceComparator.instance.compare(remoteSeq, newRemoteSeq) < 0)
            remoteSeq = newRemoteSeq;
    }

    public T consume(Metadata<T> metadata) {
        return metadata.getData();
    }






    @Override
    public String toString() {
        return "Controller:\t"
                + "DataId = " + dataId + "\t"
                + "LocalSeqNo = " + localSeq + "\t"
                + "RmoteSeqNo = " + remoteSeq;
    }

    public long getSmoothedRTT() {
        return rttHandler.getSmoothedRTT();
    }

    public long getRTTVariation() {
        return rttHandler.getRTTVariation();
    }

}
