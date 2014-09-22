package net.ddns.mucaho.jnetrobust.util;

import net.ddns.mucaho.jnetrobust.Logger;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.ProtocolListener;
import net.ddns.mucaho.jnetrobust.controller.DebugController;
import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;

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
    private final UnreliableQueue<Packet> inQueue;
    private final UnreliableQueue<Packet> outQueue;
    private final RetransmissionController protocol;
    private final boolean shouldRetransmit;

    public TestHost(TestHostListener<T> hostListener, TestHostDataGenerator<T> dataGenerator,
                    UnreliableQueue<Packet> inQueue, UnreliableQueue<Packet> outQueue, boolean retransmit,
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
        Packet inPkg;
        while ((inPkg = inQueue.poll()) != null) {
            receive(inPkg);
        }
    }

    protected void receive(Packet packet) {
        Queue<T> values = consume(packet);
        for (T value: values)
            hostListener.notifyReceived(value);
    }

    @SuppressWarnings("unchecked")
    protected Queue<T> consume(Packet packet) {
        //System.out.println("YYY"+packet.data.getDynamicReferences().size());
        Queue<T> outQueue = new LinkedList<T>();

        protocol.consume(packet);
        MultiKeyValue multiKeyValue = protocol.receive(packet);
        while (multiKeyValue != null) {
            outQueue.add((T) protocol.consume(multiKeyValue));
            multiKeyValue = protocol.receive(packet);
        }

        return outQueue;
    }


    public void send() {
        send(produce());
    }

    protected Packet produce() {
        Packet packet = protocol.produce();
        T data = dataGenerator.generateData();
        protocol.send(packet, protocol.produce(data));
        return packet;
    }

    @SuppressWarnings("unchecked")
    protected void send(Packet packet) {
        //System.out.println("WWW"+packet.data.getDynamicReferences().size());
        outQueue.offer(packet);
        for (MultiKeyValue data: packet.getDatas())
            hostListener.notifySent((T) data.getValue());
    }

    @SuppressWarnings("unchecked")
    public void retransmit() {
        if (!shouldRetransmit)
            return;

        Packet packet = protocol.produce();
        Collection<? extends MultiKeyValue> retransmits = protocol.retransmit();
        for (MultiKeyValue retransmit : retransmits) {
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
            System.out.println();
        }
    }

}
