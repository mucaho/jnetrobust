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
import com.github.mucaho.jnetrobust.util.SystemClock;

import java.nio.ByteBuffer;
import java.util.List;

public class ProcessingController implements SystemClock {
    private short localTransmissionId = Short.MIN_VALUE;
    private final SentMapControl sentMapControl;
    private final AckedMapControl ackedMapControl;
    private final RTTHandler rttHandler;
    private final RetransmissionControl retransmissionControl;
    private final CongestionControl congestionControl;

    private short remoteTransmissionId = Short.MIN_VALUE;
    private final AckBitsControl ackBitsControl;

    private short dataId = Short.MIN_VALUE;
    private final ReceivedMapControl receivedMapControl;
    private final NewestReceivedControl newestReceivedControl;

    private long timeNow = -1L;

    public ProcessingController(ProtocolListener listener, ProtocolConfig config) {

        // sending side

        sentMapControl = new SentMapControl(listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout(), this) {
            @Override
            protected void notifyAcked(Short transmissionId, Segment ackedSegment, boolean directlyAcked) {
                super.notifyAcked(transmissionId, ackedSegment, directlyAcked);

                acknowledge(transmissionId, ackedSegment, directlyAcked);
            }
        };
        ackedMapControl = new AckedMapControl(config.getPacketQueueLimit(), config.getPacketOffsetLimit(),
                config.getPacketRetransmitLimit(), config.getPacketQueueTimeout(), this);

        rttHandler = new RTTHandler(config.getK(), config.getG());
        retransmissionControl = new RetransmissionControl(sentMapControl.getValues(), ackedMapControl.getValues(),
                listener, config.getAutoRetransmitMode(), config.getNDupAck());
        congestionControl = new CongestionControl(sentMapControl.getValues(), ackedMapControl.getValues(), rttHandler);

        // receiving side

        ackBitsControl = new AckBitsControl();

        receivedMapControl = new ReceivedMapControl(Short.MIN_VALUE, listener, config.getPacketQueueLimit(),
                config.getPacketOffsetLimit(), config.getPacketRetransmitLimit(), config.getPacketQueueTimeout(), this);
        newestReceivedControl = new NewestReceivedControl(listener);
    }

    public void setTimeNow(long timeNow) {
        // let all applied timestamps and time calculations use the exact same timeNow measurement
        this.timeNow = timeNow;
    }

    @Override
    public long getTimeNow() {
        return timeNow;
    }

    public Packet produce() {
        Packet packet = new Packet();

        // apply remote transmissionId
        packet.setTransmissionAck(remoteTransmissionId);

        // apply remote precedingTransmissionIds
        packet.setPrecedingTransmissionAcks(ackBitsControl.getAckRemoteBits());

        return packet;
    }

    public Segment produce(ByteBuffer data) {
        // increment unique data id; apply unique data id
        return new Segment(++dataId, data);
    }

    public List<Segment> retransmit() {
        // update outdated not acked packets
        List<Segment> retransmits = retransmissionControl.getTimedoutRetransmits(rttHandler.getRTO(), dataId, timeNow);
        if (!retransmits.isEmpty()) {
            rttHandler.backoff(timeNow);
        } else if (!rttHandler.isBackedOff()) {
            retransmits = retransmissionControl.getFastRetransmits(rttHandler.getVTO(), dataId);
        }
        return retransmits;
    }

    public void send(Packet packet, List<Segment> segments) {
        // discard old sent entries in internal datastructures
        sentMapControl.discardEntries();

        for (int i = 0, l = segments.size(); i < l; ++i) {
            Segment segment = segments.get(i);

            // save segment to send into internal data structures
            send(segment);

            // assign segment to packet
            packet.addLastSegment(segment);
            segment.setPacketId(packet.hashCode());
        }
    }

    private void send(Segment segment) {
        // update newest sent time
        retransmissionControl.updateSentTime(segment, timeNow);

        // increment local transmissionId; add pending, local transmissionId
        sentMapControl.addToSent(++localTransmissionId, segment);
    }

    private void acknowledge(Short transmissionId, Segment ackedSegment, boolean directlyAcked) {
        if (ackedSegment != null && directlyAcked)
            rttHandler.updateRTT(ackedSegment.getNewestSentTime(), timeNow); // update RTT

        if (transmissionId != null && ackedSegment != null) {
            // update acked time
            retransmissionControl.setAcknowledgedTime(ackedSegment, timeNow);

            // discard old acked entries in internal data structures
            receivedMapControl.discardEntries();
            // add acked, local transmissionId
            ackedMapControl.addToAcked(transmissionId, ackedSegment);
        }

        // FIXME
        System.out.println(congestionControl.nextPacketSize(timeNow));
    }

    public void consume(Packet packet) {
        // remove pending, local transmissionIds
        sentMapControl.removeFromSent(packet.getTransmissionAck(), packet.getPrecedingTransmissionAcks());
    }

    public Segment receive(Packet packet) {
        // remove segment from packet assignment
        Segment segment = packet.removeFirstSegment();

        if (segment != null)
            // save received segment into internal data structures
            receive(segment);
        else
            // emit newest, remote data after packet is empty
            newestReceivedControl.emitNewestReceived();

        return segment;
    }

    private void receive(Segment segment) {
        short newRemoteTransmissionId = segment.getLastTransmissionId();

        // add received, remote transmissionIds
        ackBitsControl.addToAck(segment.getTransmissionIds(), remoteTransmissionId);

        // update newest, remote data
        newestReceivedControl.refreshNewestReceived(segment);

        // add received, remote dataIds
        receivedMapControl.addToReceived(segment);
        // discard old received entries in internal data structures
        receivedMapControl.discardEntries();
        // remove received, remote dataIds from tail
        receivedMapControl.removeFromTail();

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
