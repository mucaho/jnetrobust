package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.MetadataUnit;
import net.ddns.mucaho.jnetrobust.control.ResponseControl;

import java.util.Collection;

public class RetransmissionController extends Controller {
    protected ResponseControl responseHandler;

    public RetransmissionController(ProtocolConfig config) {
        super(config);
        responseHandler = new ResponseControl(pendingMapHandler.getMap().values());
    }

    @Override
    public void send(ProtocolUnit packet, MetadataUnit metadata) {
        // Update last modified time
        responseHandler.resetPendingTime(metadata);

        super.send(packet, metadata);
    }

    public Collection<? extends MetadataUnit> retransmit() {
        // Update outdated not acked packets
        Collection<MetadataUnit> retransmits = responseHandler.updatePendingTime(rttHandler.getRTO());
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }
}
