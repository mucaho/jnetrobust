/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.control.*;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.util.IdComparator;

public class Controller<T> {
    protected short dataId = Short.MIN_VALUE;
    protected ReceivedMapControl<T> receivedMapHandler;

    protected short localTransmissionId = Short.MIN_VALUE;
    protected PendingMapControl<T> pendingMapHandler;

    protected short remoteTransmissionId = Short.MIN_VALUE;
    protected ReceivedBitsControl receivedBitsHandler;

    protected RTTControl rttHandler;


    public Controller(ProtocolConfig<T> config) {
        pendingMapHandler = new PendingMapControl<T>(config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
                if (ackedMetadata != null && directlyAcked)
                    rttHandler.updateRTT(ackedMetadata.getTime()); // update RTT

                super.notifyAcked(ackedMetadata, directlyAcked);
            }
        };

        receivedBitsHandler = new ReceivedBitsControl(IdComparator.instance);
        receivedMapHandler = new ReceivedMapControl<T>(dataId, config.listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

        rttHandler = new RTTControl(config.getK(), config.getG());
    }



    public Packet<T> produce() {
        Packet<T> packet = new Packet<T>();

        // apply remote transmissionId
        packet.setTransmissionAck(remoteTransmissionId);

        // apply remote precedingTransmissionIds
        packet.setPrecedingTransmissionAcks((int) receivedBitsHandler.getReceivedRemoteBits());

        return packet;
    }

    public Metadata<T> produce(T data) {
        // apply unique data id; increment unique data id
        return new Metadata<T>(++dataId, data);
    }

    public void send(Packet<T> packet, Metadata<T> metadata) {
        // increment local transmissionId
        localTransmissionId++;

        // add pending, local transmissionIds
        pendingMapHandler.addToPending(localTransmissionId, metadata);


        packet.addLastMetadata(metadata);
    }



    public void consume(Packet<T> packet) {
        // remove pending, local transmissionIds
        pendingMapHandler.removeFromPending(packet.getTransmissionAck(), packet.getPrecedingTransmissionAcks());
    }

    public Metadata<T> receive(Packet<T> packet) {
        Metadata<T> metadata = packet.removeFirstMetadata();
        if (metadata != null)
            receive(metadata);

        return metadata;
    }

    private void receive(Metadata<T> metadata) {
        short newRemoteTransmissionId = metadata.getLastTransmissionId();

        // add received, remote transmissionIds
        receivedBitsHandler.addToReceived(metadata.getTransmissionIds(), remoteTransmissionId);

        // add received, remote dataIds
        receivedMapHandler.addToReceived(metadata);

        // change remote transmissionId
        if (IdComparator.instance.compare(remoteTransmissionId, newRemoteTransmissionId) < 0)
            remoteTransmissionId = newRemoteTransmissionId;
    }

    public T consume(Metadata<T> metadata) {
        return metadata.getData();
    }






    @Override
    public String toString() {
        return "Controller:\t"
                + "DataId = " + dataId + "\t"
                + "LocalTransmissionId = " + localTransmissionId + "\t"
                + "RemoteTransmissionId = " + remoteTransmissionId;
    }

    public long getSmoothedRTT() {
        return rttHandler.getSmoothedRTT();
    }

    public long getRTTVariation() {
        return rttHandler.getRTTVariation();
    }

}
