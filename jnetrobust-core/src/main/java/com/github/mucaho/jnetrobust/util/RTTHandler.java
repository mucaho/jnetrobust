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
 * RTTVAR += (| SRTT - R' | - RTTVAR) * beta
 * <p/>
 * alpha = 1/8 = 0.125
 * beta = 1/4 = 0.25
 * K = 4
 * G <= 100ms
 * <p/>
 * RTO = SRTT + max (G, K*RTTVAR)
 * RTO = min (RTO, 60s)
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

    public long getRTO() { // retransmissionTimeOut
        return rto * backoffFactor;
    }

    public void backoff() {
        lastBackoffTime = System.currentTimeMillis();
        backoffFactor++;
    }

    public void updateRTT(long packetTimestamp) {
        long rttDelta = System.currentTimeMillis() - packetTimestamp - srtt;

        if (srtt == 0 || rttvar == 0) { // first RTT received
            srtt = rttDelta;
            rttvar = rttDelta / 2;
        } else {
            srtt += rttDelta * ALPHA;
            rttvar += (Math.abs(rttDelta) - rttvar) * BETA;
        }

        rto = Math.min(srtt + Math.max(G, K * rttvar), 60L * 1000);

        if (lastBackoffTime != null && packetTimestamp >= lastBackoffTime) {
            lastBackoffTime = null;
            backoffFactor = 1;
        }
    }

}
