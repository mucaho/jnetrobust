package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.controller.RetransmissionController;
import net.ddns.mucaho.jnetrobust.data.Data;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.DebugUDPListener;
import net.ddns.mucaho.jnetrobust.util.Logger;
import net.ddns.mucaho.jnetrobust.util.UDPListener;

import java.util.Collection;


public class Protocol {
    public static interface ProtocolListener {

    }

    private final ProtocolListener protocolListener;
    private final RetransmissionController controller;

    public Protocol(ProtocolListener protocolListener) {
        this.protocolListener = protocolListener;

        UDPListener udpListener = new UDPListener() {
        };
        this.controller = new RetransmissionController(new Config(udpListener));
    }

    public Protocol(ProtocolListener protocolListener, String name, Logger logger) {
        this.protocolListener = protocolListener;

        UDPListener udpListener = new DebugUDPListener(name, logger) {
        };
        this.controller = new RetransmissionController(new Config(udpListener));
    }

    public synchronized Packet send(Object data) {
        Collection<? extends MultiKeyValue> retransmits = controller.retransmit();
        for (MultiKeyValue retransmit : retransmits) {
            internalSend(protocol.send(retransmit));
            System.out.println("[" + hostName + "-RETRANSMIT]: " + retransmit.getValue().toString());
        }
        retransmitQueue.clear();
    }

    public synchronized Data receive(Packet pkg) {

    }
}
