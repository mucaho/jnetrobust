package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.IdData;
import net.ddns.mucaho.jnetrobust.IdPacket;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.Logger;

import java.util.Collection;

public class DebugController extends RetransmissionController {
    private final String name;
    private final Logger logger;

    public DebugController(Config config, String name) {
        this(config, name, new Logger() {
            @Override
            public void log(String... texts) {
                for (String text : texts)
                    System.out.print(text + "\t");
                System.out.println();
            }
        });
    }

    public DebugController(Config config, String name, Logger logger) {
        super(config);
        this.name = name;
        this.logger = logger;
    }

    @Override
    public IdPacket send(short dataId, Packet packet) {
        Object value = packet.getData().getValue();
        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(dataId),
                value != null ? value.toString() : "null");

        return super.send(dataId, packet);
    }

    @Override
    public IdData receive(short dataId, Object value) {
        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(dataId),
                value != null ? value.toString() : "null");

        return super.receive(dataId, value);
    }

    @Override
    public Collection<? extends MultiKeyValue> retransmit() {
        Collection<? extends MultiKeyValue> retransmits = super.retransmit();
        for (MultiKeyValue retransmit: retransmits)
            logger.log(name, Logger.LoggingEvent.RETRANSMIT.toString(), String.valueOf(dataId),
                    retransmit.getValue() != null ? retransmit.getValue().toString() : "null");
        return retransmits;
    }
}
