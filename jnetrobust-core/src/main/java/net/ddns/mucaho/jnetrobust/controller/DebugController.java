package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.MetadataUnit;
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
    public MetadataUnit produce(Object data) {
        MetadataUnit out = super.produce(data);
        Object value = out.getValue();

        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(out.getStaticReference()),
                value != null ? value.toString() : "null");

        return out;
    }

    @Override
    public Object consume(MetadataUnit metadata) {
        Object value = metadata.getValue();

        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(metadata.getStaticReference()),
                value != null ? value.toString() : "null");

        return super.consume(metadata);
    }

    @Override
    public Collection<? extends MetadataUnit> retransmit() {
        Collection<? extends MetadataUnit> retransmits = super.retransmit();
        for (MetadataUnit retransmit: retransmits)
            logger.log(name, Logger.LoggingEvent.RETRANSMIT.toString(), String.valueOf(retransmit.getStaticReference()),
                    retransmit.getValue() != null ? retransmit.getValue().toString() : "null");

        return retransmits;
    }
}
