package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.controller.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.controller.ReceivedMapControl.TransmissionOrderListener;

public class UDPListener implements TransmissionSuccessListener, TransmissionOrderListener {

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
