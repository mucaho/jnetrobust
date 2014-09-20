package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.control.PendingMapControl;
import net.ddns.mucaho.jnetrobust.control.RTTControl;
import net.ddns.mucaho.jnetrobust.control.ReceivedBitsControl;
import net.ddns.mucaho.jnetrobust.control.ReceivedMapControl;
import net.ddns.mucaho.jnetrobust.IdData;
import net.ddns.mucaho.jnetrobust.IdPacket;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

public class Controller {
    protected short dataId = Short.MIN_VALUE;
    protected ReceivedMapControl receivedMapHandler;

    protected short localSeq = Short.MIN_VALUE;
    protected PendingMapControl pendingMapHandler;

    protected short remoteSeq = Short.MIN_VALUE;
    protected ReceivedBitsControl receivedBitsHandler;

    protected RTTControl rttHandler;


    public Controller(Config config) {
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


    public IdPacket send(Object data) {
        // adapt unique data id
        MultiKeyValue multiRef = new MultiKeyValue(++dataId, data);
        return send(multiRef);
    }

    public IdPacket send(MultiKeyValue data) {
        // adapt local seq
        localSeq++;

        // Handle local sequences
        pendingMapHandler.addToPending(localSeq, data);

        // Update last modified time
        data.updateTime();

//		System.out.print("C");
//		for (Short ref: data.getDynamicReferences())
//			System.out.print("["+ref+"]");
        Packet pkg = new Packet();
        pkg.setData(data);
        pkg.setAck(remoteSeq);
        pkg.setLastAcks((int) receivedBitsHandler.getReceivedRemoteBits());

        return send(data.getStaticReference(), pkg);
    }

    private IdPacket idPacket = new IdPacket();
    private IdPacket idPacketOut = IdPacket.immutablePacket(idPacket);
    public IdPacket send(short dataId, Packet packet) {
        idPacket.setDataId(dataId);
        idPacket.setPacket(packet);
        return idPacketOut;
    }



    public IdData receive(Packet pkg) {
        short newRemoteSeq = pkg.getData().getLastDynamicReference();

        // Handle local sequences
        pendingMapHandler.removeFromPending(pkg.getAck(), pkg.getLastAcks());

        // Handle remote sequences
        receivedBitsHandler.addToReceived(pkg.getData().getDynamicReferences(), remoteSeq);
        receivedMapHandler.addToReceived(pkg.getData());


        // adapt remote seq
        if (SequenceComparator.instance.compare(remoteSeq, newRemoteSeq) < 0)
            remoteSeq = newRemoteSeq;

        return receive(pkg.getData());
    }


    public IdData receive(MultiKeyValue multiKeyValue) {
        return receive(multiKeyValue.getStaticReference(), multiKeyValue.getValue());
    }

    private IdData idData = new IdData();
    private IdData idDataOut = IdData.immutableData(idData);
    public IdData receive(short dataId, Object value) {
        idData.setDataId(dataId);
        idData.setData(value);
        return idDataOut;
    }



        @Override
    public String toString() {
        return "UDPHandler:\t"
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
