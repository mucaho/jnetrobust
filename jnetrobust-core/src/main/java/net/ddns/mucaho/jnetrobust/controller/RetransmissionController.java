package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.ResponseControl;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;

import java.util.Collection;

public class RetransmissionController extends Controller {
    protected ResponseControl responseHandler;

    public RetransmissionController(ProtocolConfig config) {
        super(config);
        responseHandler = new ResponseControl(pendingMapHandler.getMap().values());
    }

    @Override
    public Packet send(MultiKeyValue data, Packet packet) {
        // Update last modified time
        responseHandler.resetPendingTime(data);

        return super.send(data, packet);
    }

    public Collection<? extends MultiKeyValue> retransmit() {
        // Update outdated not acked packets
        Collection<MultiKeyValue> retransmits = responseHandler.updatePendingTime(rttHandler.getRTO());
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }
}
