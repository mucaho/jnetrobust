package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;

import java.util.Collection;

public class TestHost<T> implements Runnable {
    public interface TestHostListener<T> {
        public void notifySent(T value);

        public void notifyReceived(T value);
    }

    public interface TestHostDataGenerator<T> {
        public T generateData();
    }

    private final TestHostListener<T> hostListener;
    private final TestHostDataGenerator<T> dataGenerator;
    private final UnreliableQueue<Packet> inQueue;
    private final UnreliableQueue<Packet> outQueue;
    private final RetransmissionController protocol;
    private final boolean shouldRetransmit;

    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<Packet> inQueue, UnreliableQueue<Packet> outQueue, boolean retransmit,
                    Config config) {
        this.hostListener = hostListener;
        this.dataGenerator = dataGenerator;
        this.protocol = new RetransmissionController(adaptConfig(config));
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.shouldRetransmit = retransmit;
    }


    public void receive() {
        Packet inPkg;
        while ((inPkg = inQueue.poll()) != null) {
            receive(inPkg);
        }
    }

    protected void receive(Packet packet) {
        T value = consume(packet);
        hostListener.notifyReceived(value);
    }

    @SuppressWarnings("unchecked")
    protected T consume(Packet packet) {
        //System.out.println("YYY"+packet.data.getDynamicReferences().size());
        return (T) protocol.receive(packet);
    }


    public void send() {
        send(produce());
    }

    protected Packet produce() {
        return protocol.send(dataGenerator.generateData());
    }

    @SuppressWarnings("unchecked")
    protected void send(Packet packet) {
        //System.out.println("WWW"+packet.data.getDynamicReferences().size());
        outQueue.offer(packet);
        hostListener.notifySent((T) packet.getData().getValue());
    }

    public void retransmit() {
        protocol.retransmit();
    }

    @Override
    public void run() {
        receive();
        if (shouldRetransmit)
            retransmit();
        send();
        System.out.println("E(X):\t" + protocol.getSmoothedRTT() +
                "\tVar(X):\t" + protocol.getRTTVariation());
        System.out.println();
    }


    private class ProtocolListenerWrapper extends UDPListener {
        private final UDPListener listener;

        private ProtocolListenerWrapper(UDPListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleTransmissionRequests(
                Collection<? extends MultiKeyValue> retransmitDatas) {
            listener.handleTransmissionRequests(retransmitDatas);
            if (shouldRetransmit) {
                for (MultiKeyValue data : retransmitDatas)
                    send(protocol.send(data));
            }
        }

        @Override
        public void handleTransmissionRequest() {
            listener.handleTransmissionRequest();
            if (shouldRetransmit)
                send();
        }

        @Override
        public void handleOrderedTransmission(Object orderedPkg) {
            listener.handleOrderedTransmission(orderedPkg);
        }

        @Override
        public void handleUnorderedTransmission(Object unorderedPkg) {
            listener.handleUnorderedTransmission(unorderedPkg);
        }

        @Override
        public void handleAckedTransmission(Object ackedPkg) {
            listener.handleAckedTransmission(ackedPkg);
        }

        @Override
        public void handleNotAckedTransmission(Object timedoutPkg) {
            listener.handleNotAckedTransmission(timedoutPkg);
        }
    }

    private Config adaptConfig(Config config) {
        return new Config(new ProtocolListenerWrapper(config.listener), config);
    }

}
