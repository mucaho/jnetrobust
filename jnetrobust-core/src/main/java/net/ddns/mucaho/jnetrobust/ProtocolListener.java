package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.control.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.control.ReceivedMapControl.TransmissionOrderListener;

public class ProtocolListener implements TransmissionSuccessListener, TransmissionOrderListener {

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