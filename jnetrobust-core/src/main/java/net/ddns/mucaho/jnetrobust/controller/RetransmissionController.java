package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.control.ResponseControl;

import java.util.Collection;

public class RetransmissionController<T> extends Controller<T> {
    protected ResponseControl<T> responseHandler;

    public RetransmissionController(ProtocolConfig<T> config) {
        super(config);
        responseHandler = new ResponseControl<T>(pendingMapHandler.getMap().values());
    }

    @Override
    public void send(Packet<T> packet, Metadata<T> metadata) {
        // Update last modified time
        responseHandler.resetPendingTime(metadata);

        super.send(packet, metadata);
    }

    public Collection<Metadata<T>> retransmit() {
        // Update outdated not acked packets
        Collection<Metadata<T>> retransmits = responseHandler.updatePendingTime(rttHandler.getRTO());
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }
}
