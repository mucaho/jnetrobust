/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.util.IdComparator;

/**
 * Configuration class which will be used to configure a {@link Protocol protocol instance}. <br></br>
 * Default configuration values are provided, but can/should be changed if needed.
 *
 * @param <T>  the user data type
 */
public class ProtocolConfig<T> {
    /**
     * The constant MAX_PACKET_QUEUE_LIMIT.
     */
    public static final int MAX_PACKET_QUEUE_LIMIT = Packet.MAX_DATAS_PER_PACKET;
    /**
     * The constant MAX_PACKET_OFFSET_LIMIT.
     */
    public static final int MAX_PACKET_OFFSET_LIMIT = IdComparator.MAX_SEQUENCE / 4;
    /**
     * The constant MAX_PACKET_RETRANSMIT_LIMIT.
     */
    public static final int MAX_PACKET_RETRANSMIT_LIMIT = Packet.MAX_DATAS_PER_PACKET;

    /**
     * Boolean indicating whether the protocol should retransmit data. Defaults to <code>true</code>.
     * If this setting is changed to <code>false</code>, more "aggressive" values should be set for
     * {@link ProtocolConfig#packetOffsetLimit packetOffsetLimit} (e.g. <code>16</code>) and
     * {@link ProtocolConfig#packetQueueLimit packetQueueLimit} (e.g. <code>16</code>).
     */
    private boolean autoRetransmit = true;
    /**
     * Positive number indicating how many <code>unacked</code> or <code>unordered</code> packaged user-datas should be
     * queued at maximum until they can be resolved. Defaults to {@link ProtocolConfig#MAX_PACKET_QUEUE_LIMIT}.
     */
    private int packetQueueLimit = MAX_PACKET_QUEUE_LIMIT;
    /**
     * Positive number indicating the maximum <code>id offset</code> of unresolved <code>unacked</code> or <code>unordered</code>
     * packaged user-datas in the internal queue. Defaults to {@link ProtocolConfig#MAX_PACKET_OFFSET_LIMIT}.
     */
    private int packetOffsetLimit = MAX_PACKET_OFFSET_LIMIT;
    /**
     * Number indicating the maximum time (in ms) <code>unacked</code> or <code>unordered</code> packaged user-data can
     * stay in the internal queue until it can be resolved. A negative number indicates that the time should not be checked.
     * Defaults to a <code>negative number</code>.
     */
    private long packetQueueTimeout = -1L;
    /**
     * Positive number indicating the maximum amount of times that individual packaged user-data may be retransmitted.
     * Defaults to {@link ProtocolConfig#MAX_PACKET_RETRANSMIT_LIMIT}.
     */
    private int packetRetransmitLimit = MAX_PACKET_RETRANSMIT_LIMIT;

    /**
     * The <code>K</code> constant used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     */
    private int K = 2;
    /**
     * The <code>G</code> constant (in ms) used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     */
    private int G = 25;

    /**
     * The Listener.
     */
    public final ProtocolListener<T> listener;

    /**
     * Instantiates a new Protocol config.
     *
     * @param listener the listener
     */
    public ProtocolConfig(ProtocolListener<T> listener) {
        super();
        this.listener = listener;
    }


    /**
     * Instantiates a new Protocol config.
     *
     * @param listener the listener
     * @param config the config
     */
    public ProtocolConfig(ProtocolListener<T> listener, ProtocolConfig<T> config) {
        super();
        this.listener = listener;
        this.packetQueueLimit = config.packetQueueLimit;
        this.packetQueueTimeout = config.packetQueueTimeout;
        this.G = config.G;
        this.K = config.K;
    }


    /**
     * Gets a positive number indicating how many <code>unacked</code> or <code>unordered</code> packaged user-datas should be
     * queued maximally until they can be resolved. Defaults to {@link ProtocolConfig#MAX_PACKET_QUEUE_LIMIT}.
     *
     * @return the packet queue limit
     */
    public int getPacketQueueLimit() {
        return packetQueueLimit;
    }

    /**
     * Sets a positive number indicating how many <code>unacked</code> or <code>unordered</code> packaged user-datas should be
     * queued maximally until they can be resolved. Defaults to {@link ProtocolConfig#MAX_PACKET_QUEUE_LIMIT}.
     * Can not be set higher than {@link ProtocolConfig#MAX_PACKET_QUEUE_LIMIT}.
     *
     * @param packetQueueLimit the packet queue limit
     */
    public void setPacketQueueLimit(int packetQueueLimit) {
        packetQueueLimit = Math.min(packetQueueLimit, MAX_PACKET_QUEUE_LIMIT);
        packetQueueLimit = Math.max(packetQueueLimit, 0);
        this.packetQueueLimit = packetQueueLimit;
    }

    /**
     * Gets a positive number indicating the maximum <code>id offset</code> of unresolved <code>unacked</code> or <code>unacked</code>
     * packaged user-datas in the internal queue. Defaults to {@link ProtocolConfig#MAX_PACKET_OFFSET_LIMIT}.
     *
     * @return the packet offset limit
     */
    public int getPacketOffsetLimit() {
        return packetOffsetLimit;
    }

    /**
     * Sets a positive number indicating the maximum <code>id offset</code> of unresolved <code>unacked</code> or <code>unacked</code>
     * packaged user-datas in the internal queue. Defaults to {@link ProtocolConfig#MAX_PACKET_OFFSET_LIMIT}.
     * Can not be set higher than {@link ProtocolConfig#MAX_PACKET_OFFSET_LIMIT}.
     *
     * @param packetOffsetLimit the packet offset limit
     */
    public void setPacketOffsetLimit(int packetOffsetLimit) {
        packetOffsetLimit = Math.min(packetOffsetLimit, MAX_PACKET_OFFSET_LIMIT);
        packetOffsetLimit = Math.max(packetOffsetLimit, 0);
        this.packetOffsetLimit = packetOffsetLimit;
    }

    /**
     * Gets a number indicating the maximum time (in ms) <code>unacked</code> or <code>unordered</code> packaged user-data can
     * stay in the internal queue until it can be resolved. A negative number indicates that the time should not be checked.
     * Defaults to a <code>negative number</code>.
     *
     * @return the packet queue timeout
     */
    public long getPacketQueueTimeout() {
        return packetQueueTimeout;
    }

    /**
     * Sets a number indicating the maximum time (in ms) <code>unacked</code> or <code>unordered</code> packaged user-data can
     * stay in the internal queue until it can be resolved. A negative number indicates that the time should not be checked.
     * Defaults to a <code>negative number</code>.
     *
     * @param packetQueueTimeout the packet queue timeout
     */
    public void setPacketQueueTimeout(long packetQueueTimeout) {
        this.packetQueueTimeout = packetQueueTimeout;
    }

    /**
     * Gets a positive number indicating the maximum amount of times that individual packaged user-data may be retransmitted.
     * Defaults to {@link ProtocolConfig#MAX_PACKET_RETRANSMIT_LIMIT}.
     *
     * @return the packet retransmit limit
     */
    public int getPacketRetransmitLimit() {
        return packetRetransmitLimit;
    }

    /**
     * Sets a positive number indicating the maximum amount of times that individual packaged user-data may be retransmitted.
     * Defaults to {@link ProtocolConfig#MAX_PACKET_RETRANSMIT_LIMIT}.
     * Can not be set higher than {@link ProtocolConfig#MAX_PACKET_RETRANSMIT_LIMIT}.
     *
     * @param packetRetransmitLimit the packet retransmit limit
     */
    public void setPacketRetransmitLimit(int packetRetransmitLimit) {
        packetRetransmitLimit = Math.min(packetRetransmitLimit, MAX_PACKET_QUEUE_LIMIT);
        packetRetransmitLimit = Math.max(packetRetransmitLimit, 0);
        this.packetRetransmitLimit = packetRetransmitLimit;
    }

    /**
     * Gets a boolean indicating whether the protocol should automatically retransmit data.
     * Defaults to <code>true</code>.
     *
     * @return boolean whether to retransmit or not
     */
    public boolean autoRetransmit() {
        return autoRetransmit;
    }

    /**
     * Sets a boolean indicating whether the protocol should automatically retransmit data.
     * Defaults to <code>true</code>.
     *
     * @param autoRetransmit boolean whether to automatically retransmit or not
     */
    public void setAutoRetransmit(boolean autoRetransmit) {
        this.autoRetransmit = autoRetransmit;
    }

    /**
     * Gets the <code>K</code> constant used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     *
     * @return the <code>K</code> constant used for computing the retransmission timeout
     */
    public int getK() {
        return K;
    }

    /**
     * Sets the <code>K</code> constant used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     *
     * @param k the <code>K</code> constant used for computing the retransmission timeout
     */
    public void setK(int k) {
        K = k;
    }

    /**
     * Gets the <code>G</code> constant (in ms) used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     *
     * @return the <code>K</code> constant used for computing the retransmission timeout
     */
    public int getG() {
        return G;
    }

    /**
     * Sets the <code>G</code> constant (in ms) used for computing the retransmission timeout.
     * <br />
     * <br />
     * The retransmission timeout (<code>RTO</code>) is calculated based on the average round-trip time (<code>RTT_avg</code>),
     * the RTT "standard deviation" (<code>RTT_stddev</code>) and constants <code>K</code> &amp; <code>G</code>.
     * <br />
     * <code>RTO = RTT_avg + max(G, K * RTT_stddev)</code>
     *
     * @param g the <code>G</code> constant used for computing the retransmission timeout
     */
    public void setG(int g) {
        G = g;
    }
}
