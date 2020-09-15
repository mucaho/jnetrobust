/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.ProtocolListener;
import com.github.mucaho.jnetrobust.control.*;
import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.RTTHandler;

import java.util.List;

public class Controller<T> {
    private short dataId = Short.MIN_VALUE;
    private final ReceivedMapControl<T> receivedMapControl;

    private short localTransmissionId = Short.MIN_VALUE;
    private final SentMapControl<T> sentMapControl;

    private short remoteTransmissionId = Short.MIN_VALUE;
    private final ReceivedBitsControl receivedBitsControl;

    private final RTTHandler rttHandler;

    private final ResponseControl<T> responseControl;

    public Controller(ProtocolListener<T> listener, ProtocolConfig config) {
        sentMapControl = new SentMapControl<T>(listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
                if (ackedMetadata != null && directlyAcked)
                    rttHandler.updateRTT(ackedMetadata.getTime()); // update RTT

                super.notifyAcked(ackedMetadata, directlyAcked);
            }
        };

        receivedBitsControl = new ReceivedBitsControl(IdComparator.instance);
        receivedMapControl = new ReceivedMapControl<T>(dataId, listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

        rttHandler = new RTTHandler(config.getK(), config.getG());

        responseControl = new ResponseControl<T>(sentMapControl.getValues(), listener, config.autoRetransmit());
    }

    public Packet<T> produce() {
        Packet<T> packet = new Packet<T>();

        // apply remote transmissionId
        packet.setTransmissionAck(remoteTransmissionId);

        // apply remote precedingTransmissionIds
        packet.setPrecedingTransmissionAcks(receivedBitsControl.getReceivedRemoteBits());

        return packet;
    }

    public Metadata<T> produce(T data) {
        // increment unique data id; apply unique data id
        return new Metadata<T>(++dataId, data);
    }

    public List<Metadata<T>> retransmit() {
        // update outdated not acked packets
        List<Metadata<T>> retransmits = responseControl.updatePendingTime(rttHandler.getRTO());
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }

    public void send(Packet<T> packet, Metadata<T> metadata) {
        // update last modified time
        responseControl.resetPendingTime(metadata);

        // increment local transmissionId; add pending, local transmissionId
        sentMapControl.addToSent(++localTransmissionId, metadata);

        packet.addLastMetadata(metadata);

        // remove metadatas that were discarded retroactively by pending map
        for (int i = packet.getMetadatas().size() - 1; i >= 0; --i) {
            if (packet.getMetadatas().get(i).getTransmissionIds().isEmpty())
                packet.remove(i);
        }
    }

    public void consume(Packet<T> packet) {
        // remove pending, local transmissionIds
        sentMapControl.removeFromSent(packet.getTransmissionAck(), packet.getPrecedingTransmissionAcks());
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
        receivedBitsControl.addToReceived(metadata.getTransmissionIds(), remoteTransmissionId);

        // add received, remote dataIds
        receivedMapControl.addToReceived(metadata);

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
