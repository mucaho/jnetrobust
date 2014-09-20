package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.control.ResponseControl;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.Config;

import java.util.Collection;

public class RetransmissionController extends Controller {
    protected ResponseControl responseHandler;

    public RetransmissionController(Config config) {
        super(config);
        responseHandler = new ResponseControl(pendingMapHandler.getMap().values());
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
