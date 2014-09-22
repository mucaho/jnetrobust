package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
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
    public MultiKeyValue produce(Object data) {
        MultiKeyValue out = super.produce(data);
        Object value = out.getValue();

        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(out.getStaticReference()),
                value != null ? value.toString() : "null");

        return out;
    }

    @Override
    public Object consume(MultiKeyValue multiKeyValue) {
        Object value = multiKeyValue.getValue();

        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(multiKeyValue.getStaticReference()),
                value != null ? value.toString() : "null");

        return super.consume(multiKeyValue);
    }

    @Override
    public Collection<? extends MultiKeyValue> retransmit() {
        Collection<? extends MultiKeyValue> retransmits = super.retransmit();
        for (MultiKeyValue retransmit: retransmits)
            logger.log(name, Logger.LoggingEvent.RETRANSMIT.toString(), String.valueOf(retransmit.getStaticReference()),
                    retransmit.getValue() != null ? retransmit.getValue().toString() : "null");

        return retransmits;
    }
}
