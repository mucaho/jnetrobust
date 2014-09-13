package net.ddns.mucaho.jnetrobust.controller;

import java.util.Collection;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

public class ResponseControl {

	public interface TransmissionRequestListener {
		public void handleTransmissionRequests(Collection<? extends MultiKeyValue> retransmitDatas);
		public void handleTransmissionRequest();
	}
	private final TransmissionRequestListener listener;
	
	private final TimeoutHandler<MultiKeyValue> pendingDataTimeoutHandler = 
		new TimeoutHandler<MultiKeyValue>();
	
	private final Collection<MultiKeyValue> pendingDatas;
	
	public ResponseControl(Collection<MultiKeyValue> pendingDatas, 
			TransmissionRequestListener listener) {
		this.listener = listener;
		this.pendingDatas = pendingDatas;
	}
	
	protected boolean updatePendingTime(long maxWaitTime) {
		Collection<MultiKeyValue> retransmits = 
			pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);

		if (!retransmits.isEmpty()) {
			listener.handleTransmissionRequests(retransmits);
			return true;
		} else {
			return false;
		}
	}
	

	private long lastSendTime = System.currentTimeMillis();
	public void resetReceivedTime() {
		this.lastSendTime = System.currentTimeMillis();
	}
	public void updateReceivedTime(long maxWaitTime) {
		if (System.currentTimeMillis() - this.lastSendTime > maxWaitTime) {
			listener.handleTransmissionRequest();
			this.resetReceivedTime();
		}
	}
}
