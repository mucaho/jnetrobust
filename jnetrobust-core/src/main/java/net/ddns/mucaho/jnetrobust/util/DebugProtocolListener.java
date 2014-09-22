package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.ProtocolListener;

public class DebugProtocolListener extends ProtocolListener {

    private final String name;
    private final Logger logger;
    private final ProtocolListener delegate;


    public DebugProtocolListener(ProtocolListener delegate, String name, Logger logger) {
        this.name = name;
        this.logger = logger;
        this.delegate = delegate;
    }

    @Override
    public void handleOrderedData(short dataId, Object orderedData) {
        logger.log(name, Logger.LoggingEvent.ORDERED.toString(), String.valueOf(dataId),
                orderedData != null ? orderedData.toString() : "null");
        delegate.handleOrderedData(dataId, orderedData);
    }

    @Override
    public void handleUnorderedData(short dataId, Object unorderedData) {
        logger.log(name, Logger.LoggingEvent.UNORDERED.toString(), String.valueOf(dataId),
                unorderedData != null ? unorderedData.toString() : "null");
        delegate.handleUnorderedData(dataId, unorderedData);
    }

    @Override
    public void handleAckedData(short dataId, Object ackedData) {
        logger.log(name, Logger.LoggingEvent.ACKED.toString(), String.valueOf(dataId),
                ackedData != null ? ackedData.toString() : "null");
        delegate.handleAckedData(dataId, ackedData);
    }

    @Override
    public void handleNotAckedData(short dataId, Object unackedData) {
        logger.log(name, Logger.LoggingEvent.NOTACKED.toString(), String.valueOf(dataId),
                unackedData != null ? unackedData.toString() : "null");
        delegate.handleNotAckedData(dataId, unackedData);
    }
}
