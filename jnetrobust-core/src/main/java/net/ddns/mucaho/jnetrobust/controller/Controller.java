package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.PendingMapControl;
import net.ddns.mucaho.jnetrobust.control.RTTControl;
import net.ddns.mucaho.jnetrobust.control.ReceivedBitsControl;
import net.ddns.mucaho.jnetrobust.control.ReceivedMapControl;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
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
            protected void notifyAcked(MultiKeyValue ackedObject, boolean directlyAcked) {
                if (ackedObject != null && directlyAcked)
                    rttHandler.updateRTT(ackedObject.getTime()); // update RTT

                super.notifyAcked(ackedObject, directlyAcked);
            }
        };

        receivedBitsHandler = new ReceivedBitsControl(SequenceComparator.instance);
        receivedMapHandler = new ReceivedMapControl(dataId, config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

        rttHandler = new RTTControl(config.getK(), config.getG());
    }



    public Packet send(Object data, Packet packet) {
        // adapt unique data id
        MultiKeyValue multiRef = new MultiKeyValue(++dataId, data);
        return send(multiRef, packet);
    }

    public Packet send(MultiKeyValue data, Packet packet) {
        if (packet == null) {
            packet = new Packet();
            packet.setAck(remoteSeq);
            packet.setLastAcks((int) receivedBitsHandler.getReceivedRemoteBits());
        }

        // adapt local seq
        localSeq++;

        // Handle local sequences
        pendingMapHandler.addToPending(localSeq, data);

//		System.out.print("C");
//		for (Short ref: data.getDynamicReferences())
//			System.out.print("["+ref+"]");
        packet.addLastData(data);

        return packet;
    }




    public Object receive(Packet packet, boolean isFirstIteration) {
        if (isFirstIteration) {
            // Handle local sequences
            pendingMapHandler.removeFromPending(packet.getAck(), packet.getLastAcks());
        }

        MultiKeyValue data = packet.removeFirstData();
        return (data != null) ? receive(data) : null;
    }

    public Object receive(MultiKeyValue data) {
        short newRemoteSeq = data.getLastDynamicReference();

        // Handle remote sequences
        receivedBitsHandler.addToReceived(data.getDynamicReferences(), remoteSeq);
        receivedMapHandler.addToReceived(data);


        // adapt remote seq
        if (SequenceComparator.instance.compare(remoteSeq, newRemoteSeq) < 0)
            remoteSeq = newRemoteSeq;

        return doReceive(data);
    }


    public Object doReceive(MultiKeyValue multiKeyValue) {
        return multiKeyValue.getValue();
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
