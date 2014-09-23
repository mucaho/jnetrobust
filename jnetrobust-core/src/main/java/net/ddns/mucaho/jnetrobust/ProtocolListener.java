package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.control.PendingMapControl.TransmissionSuccessListener;
import net.ddns.mucaho.jnetrobust.control.ReceivedMapControl.TransmissionOrderListener;

public class ProtocolListener<T> implements TransmissionSuccessListener<T>, TransmissionOrderListener<T> {

    @Override
    public void handleOrderedData(short dataId, T orderedData) {
    }

    @Override
    public void handleUnorderedData(short dataId, T unorderedData) {
    }

    @Override
    public void handleAckedData(short dataId, T ackedData) {
    }

    @Override
    public void handleNotAckedData(short dataId, T unackedData) {
    }
}
