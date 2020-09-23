/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.util.RTTHandler;

import java.util.NavigableSet;

public class CongestionControl {
    private static final int BOTTLENECK_BANDWIDTH_FILTER_LENGTH = 10;
    private static final int ROUND_TRIP_PROPAGATION_DELAY_EXPIRATION = 10; // in [s]

    private final NavigableSet<Segment> sentSegments;
    private final NavigableSet<Segment> ackedSegments;
    private final RTTHandler rttHandler;

    public CongestionControl(NavigableSet<Segment> sentSegments, NavigableSet<Segment> ackedSegments, RTTHandler rttHandler) {
        this.sentSegments = sentSegments;
        this.ackedSegments = ackedSegments;
        this.rttHandler = rttHandler;
    }

    public int nextPacketSize(long timeNow) {
        double minRTProp = 1d; // in [s], 1s
        double maxBtlBw = 8d * 1000; // in [bps], 8KB
        int btlBwCount = 0;

        long currMinRTT = Long.MAX_VALUE;
        long prevMinRTT = Long.MAX_VALUE;
        long currMaxRTT = Long.MIN_VALUE;
        long prevMaxRTT = Long.MIN_VALUE;
        long currMinTime = timeNow - rttHandler.getSmoothedRTT();
        long prevMinTime = currMinTime - rttHandler.getSmoothedRTT();

        Integer packetId = null;
        Integer previousPacketId = null;
        int packetSize = 0;
        Segment previousSegment = null;
        Segment segment = ackedSegments.isEmpty() ? null : ackedSegments.last();
        while (segment != null) {
            packetId = segment.getPacketId();

            if (previousSegment != null && !packetId.equals(previousPacketId)) {
                long ackedTime = previousSegment.getAckedTime();
                long rtt = ackedTime - previousSegment.getNewestSentTime();
                double ackedRate = (double) packetSize / rtt / 1000;

                // update maxBtlBw
                if (++btlBwCount <= BOTTLENECK_BANDWIDTH_FILTER_LENGTH && ackedRate > maxBtlBw) {
                    maxBtlBw = ackedRate;
                }

                // update minRTProp
                double rttInS = (double) rtt / 1000;
                if (ackedTime > timeNow - ROUND_TRIP_PROPAGATION_DELAY_EXPIRATION*1000 && rttInS < minRTProp) {
                    minRTProp = rttInS;
                }

                // update min/max rtts
                if (ackedTime >= prevMinTime) {
                    if (ackedTime >= currMinTime) {
                        currMinRTT = Math.min(rtt, currMinRTT);
                        currMaxRTT = Math.max(rtt, currMaxRTT);
                    } else {
                        prevMinRTT = Math.min(rtt, prevMinRTT);
                        prevMaxRTT = Math.max(rtt, prevMaxRTT);
                    }
                }

                packetSize = 0;
            } else {
                packetSize += segment.getSize();
            }

            previousPacketId = packetId;
            previousSegment = segment;
            segment = ackedSegments.lower(segment);
        }

        if (prevMinRTT == Long.MAX_VALUE) prevMinRTT = currMinRTT;
        if (prevMaxRTT == Long.MIN_VALUE) prevMaxRTT = currMaxRTT;
        long deltaMinRTT = currMinRTT - prevMinRTT;
        long deltaMaxRTT = currMaxRTT - prevMaxRTT;
        double sign = sign(deltaMinRTT, deltaMaxRTT);
        if (deltaMinRTT == 0) deltaMinRTT = 1L;
        if (deltaMaxRTT == 0) deltaMaxRTT = 1L;
        double absInvDeltaMinRTT = Math.abs((double) 1 / deltaMinRTT);
        double absInvDeltaMaxRTT = Math.abs((double) 1 / deltaMaxRTT);

        double backoffProb = 1 - Math.exp(- sign * absInvDeltaMinRTT / absInvDeltaMaxRTT);
        double increaseProb = 1 - Math.exp(- sign * absInvDeltaMaxRTT / absInvDeltaMinRTT);

        boolean doBackoff = Math.random() < backoffProb;
        boolean doIncrease = Math.random() < increaseProb;
        if (doBackoff && doIncrease) {
            if (backoffProb > increaseProb) {
                doIncrease = false;
            } else {
                doBackoff = false;
            }
        }

        int actualInFlight = 0;
        segment = sentSegments.isEmpty() ? null : sentSegments.first();
        while (segment != null) {
            actualInFlight += segment.getSize();

            segment = sentSegments.higher(segment);
        }
        int maxInFlight = doBackoff
                ? (int) (0.8 * maxBtlBw * minRTProp)
                : (int) (maxBtlBw * minRTProp);
        int leftInFlight = maxInFlight - actualInFlight;

        // FIXME: dont update BtlBw if appplication-limited

        // TODO: figure out how to measure packet send rate with holes in both acked and sent datas,
        // maybe compute this in controller on each send
        double sendRate = 60; // in [pps]
        int maxNextPacketSize = doIncrease
                ? (int) (1.2d * maxBtlBw / sendRate)
                : (int) (maxBtlBw / sendRate);

        return Math.min(maxNextPacketSize, leftInFlight) - ProtocolConfig.MOST_COMMON_LOWER_STACK_HEADER_SIZE;
    }

    private static int sign(long a, long b) {
        return Long.signum(a) == Long.signum(b)
                ? Long.signum(a)
                : Long.signum(Math.max(Math.abs(a), Math.abs(b)));
    }
}
