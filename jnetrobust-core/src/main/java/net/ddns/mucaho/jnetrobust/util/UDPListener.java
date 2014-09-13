package net.ddns.mucaho.jnetrobust.util;

import java.util.Collection;

import net.ddns.mucaho.jnetrobust.controller.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.controller.ReceivedMapControl.TransmissionOrderListener;
import net.ddns.mucaho.jnetrobust.controller.ResponseControl.TransmissionRequestListener;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;

public class UDPListener implements TransmissionSuccessListener, TransmissionOrderListener, 
	TransmissionRequestListener {

	@Override
	public void handleTransmissionRequests(Collection<? extends MultiKeyValue> retransmitDatas) {
	}

	@Override
	public void handleTransmissionRequest() {
	}

	@Override
	public void handleOrderedTransmission(Object orderedPkg) {
	}

	@Override
	public void handleUnorderedTransmission(Object unorderedPkg) {
	}

	@Override
	public void handleAckedTransmission(Object ackedPkg) {
	}

	@Override
	public void handleNotAckedTransmission(Object timedoutPkg) {
	}
}
