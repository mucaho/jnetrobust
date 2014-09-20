package net.ddns.mucaho.jnetrobust.util;

public class DebugUDPListener extends UDPListener {

    private final String name;
    private final Logger logger;

    public DebugUDPListener(String name) {
        this(name, new Logger() {
            @Override
            public void log(String... texts) {
                for (String text : texts)
                    System.out.print(text + "\t");
                System.out.println();
            }
        });
    }

    public DebugUDPListener(String name, Logger logger) {
        this.name = name;
        this.logger = logger;
    }

    @Override
    public void handleOrderedTransmission(short dataId, Object orderedData) {
        logger.log(name, Logger.LoggingEvent.ORDERED.toString(), String.valueOf(dataId),
                orderedData != null ? orderedData.toString() : "null");
    }

    @Override
    public void handleUnorderedTransmission(short dataId, Object unorderedData) {
        logger.log(name, Logger.LoggingEvent.UNORDERED.toString(), String.valueOf(dataId),
                unorderedData != null ? unorderedData.toString() : "null");
    }

    @Override
    public void handleAckedTransmission(short dataId, Object ackedData) {
        logger.log(name, Logger.LoggingEvent.ACKED.toString(), String.valueOf(dataId),
                ackedData != null ? ackedData.toString() : "null");
    }

    @Override
    public void handleNotAckedTransmission(short dataId, Object unackedData) {
        logger.log(name, Logger.LoggingEvent.NOTACKED.toString(), String.valueOf(dataId),
                unackedData != null ? unackedData.toString() : "null");
    }
}
