/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

/**
 * SRTT = (1 - alpha) * SRTT + alpha * R'
 * SRTT += ( R' - SRTT ) * alpha;
 * <p/>
 * RTTVAR = (1 - beta) * RTTVAR + beta * |SRTT - R'|
 * RTTVAR = RTTVAR - beta * RTTVAR + beta * |SRTT - R'|
 * RTTVAR += (| SRTT - R' | - RTTVAR) * beta
 * <p/>
 * alpha = 1/8 = 0.125
 * beta = 1/4 = 0.25
 * K = 4
 * G <= 100ms
 * <p/>
 * RTO = SRTT + max (G, K*RTTVAR)
 * RTO = min( max(RTO, 200ms), 60s )
 * <br/>
 * Everytime timer expires, RTO += RTO as linear backoff (or more conservatively RTO *= 2 as exponential backoff),
 * until the RTT of the newly retransmitted message arrives
 */
public final class RTTHandler {
    private static final float ALPHA = 1f / 8f;
    private static final float BETA = 1f / 4f;

    private final int K;
    private final int G; // in ms


    public RTTHandler(int k, int g) {
        super();
        K = k;
        G = g;
    }

    private long rto = 1000; // or more conservatively 3000
    private long vto = rto / 10;

    private Long lastBackoffTime = null;
    private int backoffFactor = 1;

    private long srtt = 0;
    private long rttvar = 0;


    public long getSmoothedRTT() {
        return srtt;
    }

    public long getRTTVariation() {
        return rttvar;
    }

    // relative retransmission ("variance") timeout
    public long getVTO() {
        // for isolated packet losses we do not need to backoff,
        // otherwise RTO will trigger for a complete stalling loss of multiple packets
        return vto;
    }

    // absolute retransmission timeout
    public long getRTO() {
        return rto * backoffFactor;
    }

    public void backoff(long timeNow) {
        lastBackoffTime = timeNow;
        backoffFactor++;
    }

    public boolean isBackedOff() {
        return lastBackoffTime != null;
    }

    public void updateRTT(long packetTimestamp, long timeNow) {
        long rttDelta = timeNow - packetTimestamp - srtt;

        if (srtt == 0 || rttvar == 0) { // first RTT received
            srtt = rttDelta;
            rttvar = rttDelta / 2;
        } else {
            srtt += rttDelta * ALPHA;
            rttvar += (Math.abs(rttDelta) - rttvar) * BETA;
        }

        long v = Math.max(G, K * rttvar);
        rto = Math.min(Math.max(srtt + v, 200L), 60L * 1000);
        vto = Math.min(v, 1000L);

        if (lastBackoffTime != null && packetTimestamp >= lastBackoffTime) {
            lastBackoffTime = null;
            backoffFactor = 1;
        }
    }

}
