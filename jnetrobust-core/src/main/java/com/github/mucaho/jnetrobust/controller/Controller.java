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
    private short localTransmissionId = Short.MIN_VALUE;
    private final SentMapControl<T> sentMapControl;

    private short remoteTransmissionId = Short.MIN_VALUE;
    private final ReceivedMapControl<T> receivedMapControl;
    private final ReceivedBitsControl receivedBitsControl;

    private final RTTHandler rttHandler;
    private final RetransmissionControl<T> retransmissionControl;

    private final NewestDataControl<T> newestDataControl;

    public Controller(ProtocolListener<T> listener, ProtocolConfig config) {

        // sending side

        sentMapControl = new SentMapControl<T>(listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Segment<T> ackedSegment, boolean directlyAcked) {
                if (ackedSegment != null && directlyAcked)
                    rttHandler.updateRTT(ackedSegment.getTime()); // update RTT

                super.notifyAcked(ackedSegment, directlyAcked);
            }
        };

        rttHandler = new RTTHandler(config.getK(), config.getG());
        retransmissionControl = new RetransmissionControl<T>(sentMapControl.getValues(), listener, config.getAutoRetransmitMode());

        // receiving side

        receivedBitsControl = new ReceivedBitsControl();
        receivedMapControl = new ReceivedMapControl<T>(Short.MIN_VALUE, listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout());

        newestDataControl = new NewestDataControl<T>(listener);
    }

    public Packet<T> produce() {
        Packet<T> packet = new Packet<T>();

        // apply remote transmissionId
        packet.setTransmissionAck(remoteTransmissionId);

        // apply remote precedingTransmissionIds
        packet.setPrecedingTransmissionAcks(receivedBitsControl.getReceivedRemoteBits());

        return packet;
    }

    public Segment<T> produce(T data) {
        // increment unique data id; apply unique data id
        return new Segment<T>(++dataId, data);
    }

    public List<Segment<T>> retransmit() {
        // update outdated not acked packets
        List<Segment<T>> retransmits = retransmissionControl.updatePendingTime(rttHandler.getRTO(), dataId);
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }

    public void send(Packet<T> packet, Segment<T> segment) {
        // update last modified time
        retransmissionControl.resetPendingTime(segment);

        // increment local transmissionId; add pending, local transmissionId
        sentMapControl.addToSent(++localTransmissionId, segment);

        packet.addLastSegment(segment);

        // remove segments that were discarded retroactively by pending map
        for (int i = packet.getSegments().size() - 1; i >= 0; --i) {
            if (packet.getSegments().get(i).getTransmissionIds().isEmpty())
                packet.remove(i);
        }
    }

    public void consume(Packet<T> packet) {
        // remove pending, local transmissionIds
        sentMapControl.removeFromSent(packet.getTransmissionAck(), packet.getPrecedingTransmissionAcks());
    }

    public Segment<T> receive(Packet<T> packet) {
        Segment<T> segment = packet.removeFirstSegment();
        if (segment != null)
            receive(segment);
        else
            // emit newest, remote data after packet is empty
            newestDataControl.emitNewestData();

        return segment;
    }

    private void receive(Segment<T> segment) {
        short newRemoteTransmissionId = segment.getLastTransmissionId();

        // update newest, remote data
        newestDataControl.refreshNewestData(segment);

        // add received, remote transmissionIds
        receivedBitsControl.addToReceived(segment.getTransmissionIds(), remoteTransmissionId);

        // add received, remote dataIds
        receivedMapControl.addToReceived(segment);

        // change remote transmissionId
        if (IdComparator.instance.compare(remoteTransmissionId, newRemoteTransmissionId) < 0)
            remoteTransmissionId = newRemoteTransmissionId;
    }

    public T consume(Segment<T> segment) {
        return segment.getData();
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
