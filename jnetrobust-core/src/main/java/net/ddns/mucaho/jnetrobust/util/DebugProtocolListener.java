package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.ProtocolListener;

public class DebugProtocolListener extends ProtocolListener {

    private final String name;
    private final Logger logger;
    private final ProtocolListener delegate;

    public DebugProtocolListener(ProtocolListener delegate, String name) {
        this(delegate, name, new Logger() {
            @Override
            public void log(String... texts) {
                for (String text : texts)
                    System.out.print(text + "\t");
                System.out.println();
            }
        });
    }

    public DebugProtocolListener(ProtocolListener delegate, String name, Logger logger) {
        this.name = name;
        this.logger = logger;
        this.delegate = delegate;
    }

    @Override
    public void handleOrderedTransmission(short dataId, Object orderedData) {
        logger.log(name, Logger.LoggingEvent.ORDERED.toString(), String.valueOf(dataId),
                orderedData != null ? orderedData.toString() : "null");
        delegate.handleOrderedTransmission(dataId, orderedData);
    }

    @Override
    public void handleUnorderedTransmission(short dataId, Object unorderedData) {
        logger.log(name, Logger.LoggingEvent.UNORDERED.toString(), String.valueOf(dataId),
                unorderedData != null ? unorderedData.toString() : "null");
        delegate.handleUnorderedTransmission(dataId, unorderedData);
    }

    @Override
    public void handleAckedTransmission(short dataId, Object ackedData) {
        logger.log(name, Logger.LoggingEvent.ACKED.toString(), String.valueOf(dataId),
                ackedData != null ? ackedData.toString() : "null");
        delegate.handleAckedTransmission(dataId, ackedData);
    }

    @Override
    public void handleNotAckedTransmission(short dataId, Object unackedData) {
        logger.log(name, Logger.LoggingEvent.NOTACKED.toString(), String.valueOf(dataId),
                unackedData != null ? unackedData.toString() : "null");
        delegate.handleNotAckedTransmission(dataId, unackedData);
    }
}
