package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

public class Controller {
	protected short dataId = Short.MIN_VALUE;
	protected ReceivedMapControl receivedMapHandler;
	
	protected short localSeq = Short.MIN_VALUE;
	protected final PendingMapControl pendingMapHandler;

	protected short remoteSeq = Short.MIN_VALUE;
	protected ReceivedBitsControl receivedBitsHandler;
	
	protected RTTControl rttHandler;


	public Controller(Config config) {		
		pendingMapHandler = new PendingMapControl(config.listener, config.packetQueueLimit, 
				config.packetQueueTimeout) {
			@Override
			protected void notifyAcked(MultiKeyValue ackedObject, boolean directlyAcked) {
				if (ackedObject != null && directlyAcked)
					rttHandler.updateRTT(ackedObject.getTime()); // update RTT

				super.notifyAcked(ackedObject, directlyAcked);
			}
		};
		
		receivedBitsHandler = new ReceivedBitsControl(SequenceComparator.instance);
		receivedMapHandler = new ReceivedMapControl(remoteSeq, config.listener, 
				config.packetQueueLimit, config.packetQueueTimeout);
		
		rttHandler = new RTTControl(config.K, config.G);
	}

	
	public synchronized Packet send(Object data) {
		MultiKeyValue multiRef = new MultiKeyValue(++dataId, data);
		return send(multiRef);
	}
	public synchronized Packet send(MultiKeyValue data) {
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
		pkg.setLastAcks((int)receivedBitsHandler.getReceivedRemoteBits());
		return pkg;
	}
	
	public synchronized Object receive(Packet pkg) {
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

	public synchronized Object receive(MultiKeyValue data) {
		return data.getValue();
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