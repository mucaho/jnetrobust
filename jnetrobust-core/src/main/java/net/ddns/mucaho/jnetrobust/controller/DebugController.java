package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.Logger;

import java.util.Collection;

public class DebugController<T> extends RetransmissionController<T> {
    private final String name;
    private final Logger logger;

    public DebugController(ProtocolConfig<T> config, String name, Logger logger) {
        super(config);
        this.name = name;
        this.logger = logger;
    }

    @Override
    public Metadata<T> produce(T data) {
        Metadata<T> out = super.produce(data);
        T value = out.getData();

        logger.log(name, Logger.LoggingEvent.SEND.toString(), String.valueOf(out.getDataId()),
                value != null ? value.toString() : "null");

        return out;
    }

    @Override
    public T consume(Metadata<T> metadata) {
        T value = metadata.getData();
        logger.log(name, Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(metadata.getDataId()),
                value != null ? value.toString() : "null");

        return super.consume(metadata);
    }

    @Override
    public Collection<Metadata<T>> retransmit() {
        Collection<Metadata<T>> retransmits = super.retransmit();
        for (Metadata<T> retransmit: retransmits)
            logger.log(name, Logger.LoggingEvent.RETRANSMIT.toString(), String.valueOf(retransmit.getDataId()),
                    retransmit.getData() != null ? retransmit.getData().toString() : "null");

        return retransmits;
    }
}
