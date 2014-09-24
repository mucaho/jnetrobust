package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.ProtocolListener;

public class DebugProtocolListener<T> extends ProtocolListener<T> {

    private final String name;
    private final Logger logger;
    private final ProtocolListener<T> delegate;


    public DebugProtocolListener(ProtocolListener<T> delegate, String name, Logger logger) {
        this.name = name;
        this.logger = logger;
        this.delegate = delegate;
    }

    @Override
    public void handleOrderedData(short dataId, T orderedData) {
        logger.log(name, Logger.LoggingEvent.ORDERED.toString(), String.valueOf(dataId),
                orderedData != null ? orderedData.toString() : "null");
        delegate.handleOrderedData(dataId, orderedData);
    }

    @Override
    public void handleUnorderedData(short dataId, T unorderedData) {
        logger.log(name, Logger.LoggingEvent.UNORDERED.toString(), String.valueOf(dataId),
                unorderedData != null ? unorderedData.toString() : "null");
        delegate.handleUnorderedData(dataId, unorderedData);
    }

    @Override
    public void handleAckedData(short dataId, T ackedData) {
        logger.log(name, Logger.LoggingEvent.ACKED.toString(), String.valueOf(dataId),
                ackedData != null ? ackedData.toString() : "null");
        delegate.handleAckedData(dataId, ackedData);
    }

    @Override
    public void handleUnackedData(short dataId, T unackedData) {
        logger.log(name, Logger.LoggingEvent.NOTACKED.toString(), String.valueOf(dataId),
                unackedData != null ? unackedData.toString() : "null");
        delegate.handleUnackedData(dataId, unackedData);    }
}
