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

import java.nio.ByteBuffer;
import java.util.List;

public class ProcessingController {
    private short dataId = Short.MIN_VALUE;
    private short localTransmissionId = Short.MIN_VALUE;
    private final SentMapControl sentMapControl;

    private short remoteTransmissionId = Short.MIN_VALUE;
    private final ReceivedMapControl receivedMapControl;
    private final ReceivedBitsControl receivedBitsControl;

    private final RTTHandler rttHandler;
    private final RetransmissionControl retransmissionControl;

    private final NewestDataControl newestDataControl;

    public ProcessingController(ProtocolListener listener, ProtocolConfig config) {

        // sending side

        sentMapControl = new SentMapControl(listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout()) {
            @Override
            protected void notifyAcked(Segment ackedSegment, boolean directlyAcked) {
                if (ackedSegment != null && directlyAcked)
                    rttHandler.updateRTT(ackedSegment.getTime()); // update RTT

                super.notifyAcked(ackedSegment, directlyAcked);
            }
        };

        rttHandler = new RTTHandler(config.getK(), config.getG());
        retransmissionControl = new RetransmissionControl(sentMapControl.getValues(), listener, config.getAutoRetransmitMode());

        // receiving side

        receivedBitsControl = new ReceivedBitsControl();
        receivedMapControl = new ReceivedMapControl(Short.MIN_VALUE, listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout());

        newestDataControl = new NewestDataControl(listener);
    }

    public Packet produce() {
        Packet packet = new Packet();

        // apply remote transmissionId
        packet.setTransmissionAck(remoteTransmissionId);

        // apply remote precedingTransmissionIds
        packet.setPrecedingTransmissionAcks(receivedBitsControl.getReceivedRemoteBits());

        return packet;
    }

    public Segment produce(ByteBuffer data) {
        // increment unique data id; apply unique data id
        return new Segment(++dataId, data);
    }

    public List<Segment> retransmit() {
        // update outdated not acked packets
        List<Segment> retransmits = retransmissionControl.updatePendingTime(rttHandler.getRTO(), dataId);
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }

    public void send(Packet packet, List<Segment> segments) {
        // save segments to send into internal data structures
        for (int i = 0, l = segments.size(); i < l; ++i)
            send(segments.get(i));

        // some segments may have been discarded retroactively due to internal queue limits, only add those that were not
        for (int i = 0, l = segments.size(); i < l; ++i) {
            Segment segment = segments.get(i);
            if (!segment.getTransmissionIds().isEmpty())
                packet.addLastSegment(segment);
            else
                System.err.println("EMPTY"); // TODO: recover localTransmissionId here
        }
    }

    private void send(Segment segment) {
        // update last modified time
        retransmissionControl.resetPendingTime(segment);

        // increment local transmissionId; add pending, local transmissionId
        sentMapControl.addToSent(++localTransmissionId, segment);
    }

    public void consume(Packet packet) {
        // remove pending, local transmissionIds
        sentMapControl.removeFromSent(packet.getTransmissionAck(), packet.getPrecedingTransmissionAcks());
    }

    public Segment receive(Packet packet) {
        Segment segment = packet.removeFirstSegment();
        if (segment != null)
            receive(segment);
        else
            // emit newest, remote data after packet is empty
            newestDataControl.emitNewestData();

        return segment;
    }

    private void receive(Segment segment) {
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

    public ByteBuffer consume(Segment segment) {
        if (segment.getData() != null) segment.getData().rewind();
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
