package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.controller.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.controller.ReceivedMapControl.TransmissionOrderListener;

public class UDPListener implements TransmissionSuccessListener, TransmissionOrderListener {

    @Override
    public void handleOrderedTransmission(short dataId, Object orderedData) {
    }

    @Override
    public void handleUnorderedTransmission(short dataId, Object unorderedData) {
    }

    @Override
    public void handleAckedTransmission(short dataId, Object ackedData) {
    }

    @Override
    public void handleNotAckedTransmission(short dataId, Object unackedData) {
    }
}
