package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.control.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.control.ReceivedMapControl.TransmissionOrderListener;

public class ProtocolListener implements TransmissionSuccessListener, TransmissionOrderListener {

    @Override
    public void handleOrderedData(short dataId, Object orderedData) {
    }

    @Override
    public void handleUnorderedData(short dataId, Object unorderedData) {
    }

    @Override
    public void handleAckedData(short dataId, Object ackedData) {
    }

    @Override
    public void handleNotAckedData(short dataId, Object unackedData) {
    }
}
