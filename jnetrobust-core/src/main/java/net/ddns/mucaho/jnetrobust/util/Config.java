package net.ddns.mucaho.jnetrobust.util;

public class Config {
    public int packetQueueLimit = SequenceComparator.MAX_SEQUENCE / 4;
    public long packetQueueTimeout = -1L;
    public int K = 2;
    public int G = 25;
    public final UDPListener listener;

    public Config(UDPListener listener) {
        super();
        this.listener = listener;
    }


    public Config(UDPListener listener, Config config) {
        super();
        this.listener = listener;
        this.packetQueueLimit = config.packetQueueLimit;
        this.packetQueueTimeout = config.packetQueueTimeout;
        this.G = config.G;
        this.K = config.K;
    }


}
