/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.control.Segment;

import java.util.*;

public final class TimeoutHandler {
    private final List<Segment> timeouts = new ArrayList<Segment>();
    private final List<Segment> timeoutsOut = Collections.unmodifiableList(timeouts);

    public List<Segment> filterTimedout(NavigableSet<Segment> datas, long timeout, long timeNow) {
        timeouts.clear();

        Segment data = datas.isEmpty() ? null : datas.first();
        while (data != null) {
            if (timeNow - data.getTime() > timeout) {
                timeouts.add(data);
            }

            data = datas.higher(data);
        }

        return timeoutsOut;
    }

    public List<Segment> filterLostInBetween(NavigableSet<Segment> segments, long relativeTimeout, int minDistance) {
        timeouts.clear();
        if (segments.isEmpty()) return timeoutsOut;

        long newestAckedSentTime = -1L;

        Integer previousPacketId = null;
        Integer packetId = null;
        long sentTime = -1L;
        boolean packetAcked = false;
        int ackedPacketDelta = 0;

        Segment segment = segments.last();
        while (segment != null) {
            // iteration loop initializers
            sentTime = segment.getNewestSentTime();
            packetId = segment.getPacketId();

            // initialize sent time to most recent acked data
            if (newestAckedSentTime < 0 && segment.getAckedTime() >= 0)
                newestAckedSentTime = segment.getNewestSentTime();

            // do not start processing until most recent acked data encountered
            if (newestAckedSentTime >= 0) {

                // determine all segments that were sent in one packet
                if (!packetId.equals(previousPacketId)) {
                    // increase packet delta if the current packet is not the starting acked packet
                    if (sentTime != newestAckedSentTime && packetAcked)
                        ackedPacketDelta++;

                    // new packet's initial acked flag depends on first segment of that packet
                    packetAcked = segment.getAckedTime() >= 0;
                } else {
                    // if at least one segment is packed in packet, whole packet is marked as acked
                    packetAcked = packetAcked || segment.getAckedTime() >= 0;
                }

                // fast-retransmit if it's too big of a delay and multiple acks for newer datas have been received already
                if (segment.getAckedTime() < 0
                        && newestAckedSentTime - sentTime > relativeTimeout
                        && ackedPacketDelta >= minDistance) {

                    timeouts.add(segment);
                }
            }

           // iteration loop updates
            previousPacketId = packetId;
            segment = segments.lower(segment);
        }

        // reverse the list, as newer datas should be last in the list
        Collections.reverse(timeouts);
        return timeoutsOut;
    }
}
