package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.ProtocolListener;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;

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
                    ProtocolConfig config) {
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


    private class ProtocolListenerWrapper extends ProtocolListener {
        private final ProtocolListener listener;

        private ProtocolListenerWrapper(ProtocolListener listener) {
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
        public void handleOrderedTransmission(Object orderedData) {
            listener.handleOrderedTransmission(orderedData);
        }

        @Override
        public void handleUnorderedTransmission(Object unorderedData) {
            listener.handleUnorderedTransmission(unorderedData);
        }

        @Override
        public void handleAckedTransmission(Object ackedData) {
            listener.handleAckedTransmission(ackedData);
        }

        @Override
        public void handleNotAckedTransmission(Object unackedData) {
            listener.handleNotAckedTransmission(unackedData);
        }
    }

    private ProtocolConfig adaptConfig(ProtocolConfig config) {
        return new ProtocolConfig(new ProtocolListenerWrapper(config.listener), config);
    }

}
