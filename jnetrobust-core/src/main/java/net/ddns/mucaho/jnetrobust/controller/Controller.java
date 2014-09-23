package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.*;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

public class Controller {
    protected short dataId = Short.MIN_VALUE;
    protected ReceivedMapControl receivedMapHandler;

    protected short localSeq = Short.MIN_VALUE;
    protected PendingMapControl pendingMapHandler;

    protected short remoteSeq = Short.MIN_VALUE;
    protected ReceivedBitsControl receivedBitsHandler;

    protected RTTControl rttHandler;


    public Controller(ProtocolConfig config) {
        pendingMapHandler = new PendingMapControl(config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Metadata ackedMetadata, boolean directlyAcked) {
                if (ackedMetadata != null && directlyAcked)
                    rttHandler.updateRTT(ackedMetadata.getTime()); // update RTT

                super.notifyAcked(ackedMetadata, directlyAcked);
            }
        };

        receivedBitsHandler = new ReceivedBitsControl(SequenceComparator.instance);
        receivedMapHandler = new ReceivedMapControl(dataId, config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

        rttHandler = new RTTControl(config.getK(), config.getG());
    }



    public Packet produce() {
        Packet packet = new Packet();
        // adapt remote seq
        packet.setAck(remoteSeq);
        // Handle remote sequences
        packet.setLastAcks((int) receivedBitsHandler.getReceivedRemoteBits());

        return packet;
    }

    public Metadata produce(Object data) {
        // Handle data id: adapt unique data id
        return new Metadata(++dataId, data);
    }

    public void send(Packet packet, Metadata metadata) {
        // adapt local seq
        localSeq++;

        // Handle local sequences
        pendingMapHandler.addToPending(localSeq, metadata);

//		System.out.print("C");
//		for (Short ref: metadata.getDynamicReferences())
//			System.out.print("["+ref+"]");

        packet.addLastMetadata(metadata);
    }



    public void consume(Packet packet) {
        // Handle local sequences
        pendingMapHandler.removeFromPending(packet.getAck(), packet.getLastAcks());
    }

    public Metadata receive(Packet packet) {
        Metadata metadata = packet.removeFirstMetadata();
        if (metadata != null)
            receive(metadata);

        return metadata;
    }

    private void receive(Metadata metadata) {
        short newRemoteSeq = metadata.getLastDynamicReference();

        // Handle remote sequences
        receivedBitsHandler.addToReceived(metadata.getDynamicReferences(), remoteSeq);
        // Handle metadata id
        receivedMapHandler.addToReceived(metadata);

        // adapt remote seq
        if (SequenceComparator.instance.compare(remoteSeq, newRemoteSeq) < 0)
            remoteSeq = newRemoteSeq;
    }

    public Object consume(Metadata metadata) {
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
