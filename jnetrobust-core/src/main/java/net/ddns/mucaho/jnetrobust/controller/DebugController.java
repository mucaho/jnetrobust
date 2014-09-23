package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.Logger;

import java.util.Collection;

public class DebugController extends RetransmissionController {
    private final String name;
    private final Logger logger;

    public DebugController(ProtocolConfig config, String name, Logger logger) {
        super(config);
        this.name = name;
        this.logger = logger;
    }

    @Override
    public Metadata produce(Object data) {
        Metadata out = super.produce(data);
        Object value = out.getData();

        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(out.getStaticReference()),
                value != null ? value.toString() : "null");

        return out;
    }

    @Override
    public Object consume(Metadata metadata) {
        Object value = metadata.getData();

        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(metadata.getStaticReference()),
                value != null ? value.toString() : "null");

        return super.consume(metadata);
    }

    @Override
    public Collection<? extends Metadata> retransmit() {
        Collection<? extends Metadata> retransmits = super.retransmit();
        for (Metadata retransmit: retransmits)
            logger.log(name, Logger.LoggingEvent.RETRANSMIT.toString(), String.valueOf(retransmit.getStaticReference()),
                    retransmit.getData() != null ? retransmit.getData().toString() : "null");

        return retransmits;
    }
}
