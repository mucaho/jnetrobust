package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.Logger;

import java.util.Collection;

public class DebugController extends RetransmissionController {
    private final String name;
    private final Logger logger;

    public DebugController(ProtocolConfig config, String name) {
        this(config, name, new Logger() {
            @Override
            public void log(String... texts) {
                for (String text : texts)
                    System.out.print(text + "\t");
                System.out.println();
            }
        });
    }

    public DebugController(ProtocolConfig config, String name, Logger logger) {
        super(config);
        this.name = name;
        this.logger = logger;
    }

    @Override
    public Packet send(MultiKeyValue data, Packet packet) {
        Object value = data.getValue();
        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(data.getStaticReference()),
                value != null ? value.toString() : "null");

        return super.send(data, packet);
    }

    @Override
    public Object doReceive(MultiKeyValue multiKeyValue) {
        Object value = multiKeyValue.getValue();
        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(multiKeyValue.getStaticReference()),
                value != null ? value.toString() : "null");

        return super.receive(multiKeyValue);
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
