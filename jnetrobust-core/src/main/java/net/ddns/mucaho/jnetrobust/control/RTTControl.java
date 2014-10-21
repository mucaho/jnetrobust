/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

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
 * <p/>
 * if timer expires, RTO *= 2, until the RTT of the newly retransmitted message arrives
 */
public class RTTControl {
    private final static float alpha = 1f / 8f;
    private final static float beta = 1f / 4f;

    private final int K;
    private final int G; //ms


    public RTTControl(int k, int g) {
        super();
        K = k;
        G = g;
    }

    private long rto = 3000;
    private Long lastBackoffTime = null;
    private long srtt = 0;
    private long rttvar = 0;


    public long getSmoothedRTT() {
        return this.srtt;
    }

    public long getRTTVariation() {
        return this.rttvar;
    }

    public long getRTO() { // retransmissionTimeOut
        if (lastBackoffTime != null) {
            return this.rto * 2;
        } else {
            return this.rto;
        }
    }

    public void backoff() {
        lastBackoffTime = System.currentTimeMillis();
    }

    public void updateRTT(long packetTimestamp) {
        long rttDelta = System.currentTimeMillis() - packetTimestamp - srtt;

        if (srtt == 0 || rttvar == 0) { // first RTT received
            srtt = rttDelta;
            rttvar = rttDelta / 2;
        } else {
            srtt += rttDelta * alpha;
            rttvar += (Math.abs(rttDelta) - rttvar) * beta;
        }

        rto = srtt + Math.max(G, K * rttvar);

        if (lastBackoffTime != null && packetTimestamp >= lastBackoffTime) {
            lastBackoffTime = null;
        }
    }

}
