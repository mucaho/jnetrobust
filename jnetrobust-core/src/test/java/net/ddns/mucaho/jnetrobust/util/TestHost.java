package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.MetadataUnit;
import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.ProtocolUnit;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class TestHost<T> implements Runnable {
    private final boolean debug;
    public interface TestHostListener<T> {
        public void notifySent(T value);
        public void notifyRetransmitted(T value);
        public void notifyReceived(T value);
    }

    public interface TestHostDataGenerator<T> {
        public T generateData();
    }

    private final TestHostListener<T> hostListener;
    private final TestHostDataGenerator<T> dataGenerator;
    private final UnreliableQueue<ProtocolUnit> inQueue;
    private final UnreliableQueue<ProtocolUnit> outQueue;
    private final RetransmissionController protocol;
    private final boolean shouldRetransmit;

    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<ProtocolUnit> inQueue, UnreliableQueue<ProtocolUnit> outQueue, boolean retransmit,
                    ProtocolConfig config, String name, boolean debug) {
        this.debug = debug;
        this.hostListener = hostListener;
        this.dataGenerator = dataGenerator;
        if (debug)
            this.protocol = new DebugController(config, name, Logger.getConsoleLogger());
        else
            this.protocol = new RetransmissionController(config);
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.shouldRetransmit = retransmit;
    }


    public void receive() {
        ProtocolUnit packet;
        while ((packet = inQueue.poll()) != null) {
            receive(packet);
        }
    }

    protected void receive(ProtocolUnit packet) {
        Queue<T> values = consume(packet);
        for (T value: values)
            hostListener.notifyReceived(value);
    }

    @SuppressWarnings("unchecked")
    protected Queue<T> consume(ProtocolUnit packet) {
        //System.out.println("YYY"+packet.data.getDynamicReferences().size());
        Queue<T> outQueue = new LinkedList<T>();

        protocol.consume(packet);
        MetadataUnit metadata = protocol.receive(packet);
        while (metadata != null) {
            outQueue.add((T) protocol.consume(metadata));
            metadata = protocol.receive(packet);
        }

        return outQueue;
    }


    public void send() {
        send(produce());
    }

    protected ProtocolUnit produce() {
        ProtocolUnit packet = protocol.produce();
        T data = dataGenerator.generateData();
        protocol.send(packet, protocol.produce(data));
        return packet;
    }

    @SuppressWarnings("unchecked")
    protected void send(ProtocolUnit packet) {
        //System.out.println("WWW"+packet.data.getDynamicReferences().size());
        outQueue.offer(packet);
        for (MetadataUnit metadata: packet.getMetadatas())
            hostListener.notifySent((T) metadata.getValue());
    }

    @SuppressWarnings("unchecked")
    public void retransmit() {
        if (!shouldRetransmit)
            return;

        ProtocolUnit packet = protocol.produce();
        Collection<? extends MetadataUnit> retransmits = protocol.retransmit();
        for (MetadataUnit retransmit : retransmits) {
            hostListener.notifyRetransmitted((T) retransmit.getValue());
            protocol.send(packet, retransmit);
        }
        send(packet);
    }

    @Override
    public void run() {
        receive();
        if (shouldRetransmit)
            retransmit();
        send();
        if (debug) {
            System.out.println("E(X):\t" + protocol.getSmoothedRTT() +
                    "\tVar(X):\t" + protocol.getRTTVariation());
        }
    }

}
