package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.Packet;
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
    private final UnreliableQueue<Packet<T>> inQueue;
    private final UnreliableQueue<Packet<T>> outQueue;
    private final RetransmissionController<T> protocol;
    private final boolean shouldRetransmit;

    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<Packet<T>> inQueue, UnreliableQueue<Packet<T>> outQueue, boolean retransmit,
                    ProtocolConfig<T> config, String name, boolean debug) {
        this.debug = debug;
        this.hostListener = hostListener;
        this.dataGenerator = dataGenerator;
        if (debug)
            this.protocol = new DebugController<T>(config, name, Logger.getConsoleLogger());
        else
            this.protocol = new RetransmissionController<T>(config);
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.shouldRetransmit = retransmit;
    }


    public void receive() {
        Packet<T> packet;
        while ((packet = inQueue.poll()) != null) {
            receive(packet);
        }
    }

    protected void receive(Packet<T> packet) {
        Queue<T> values = consume(packet);
        for (T value: values)
            hostListener.notifyReceived(value);
    }

    protected Queue<T> consume(Packet<T> packet) {
        Queue<T> outQueue = new LinkedList<T>();

        protocol.consume(packet);
        Metadata<T> metadata = protocol.receive(packet);
        while (metadata != null) {
            outQueue.add(protocol.consume(metadata));
            metadata = protocol.receive(packet);
        }

        return outQueue;
    }


    public void send() {
        send(produce());
    }

    protected Packet<T> produce() {
        Packet<T> packet = protocol.produce();
        T data = dataGenerator.generateData();
        protocol.send(packet, protocol.produce(data));
        return packet;
    }

    protected void send(Packet<T> packet) {
        outQueue.offer(packet);
        for (Metadata<T> metadata: packet.getMetadatas())
            hostListener.notifySent(metadata.getData());
    }

    public void retransmit() {
        if (!shouldRetransmit)
            return;

        Packet<T> packet = protocol.produce();
        Collection<Metadata<T>> retransmits = protocol.retransmit();
        for (Metadata<T> retransmit : retransmits) {
            hostListener.notifyRetransmitted(retransmit.getData());
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
