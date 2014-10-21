/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.util.IdComparator;

/**
 * Configuration class which will be used to configure a {@link Protocol protocol instance}. <br></br>
 * Default configuration values are provided, but can/should be changed if needed.
 *
 * @param <T>  the user data type
 * @jnetrobust.api
 */
public class ProtocolConfig<T> {
    /**
     * The constant MAX_PACKET_QUEUE_LIMIT.
     */
    public final static int MAX_PACKET_QUEUE_LIMIT = Packet.MAX_DATAS_PER_PACKET;
    /**
     * The constant MAX_PACKET_OFFSET_LIMIT.
     */
    public final static int MAX_PACKET_OFFSET_LIMIT = IdComparator.MAX_SEQUENCE / 4;
    /**
     * The constant MAX_PACKET_RETRANSMIT_LIMIT.
     */
    public final static int MAX_PACKET_RETRANSMIT_LIMIT = Packet.MAX_DATAS_PER_PACKET;

    /**
     * Boolean indicating whether the protocol should retransmit data. Defaults to <code>true</code>.
     * If this setting is changed to <code>false</code>, more "aggressive" values should be set for
     * {@link ProtocolConfig#packetOffsetLimit packetOffsetLimit} (e.g. <code>16</code>) and
     * {@link ProtocolConfig#packetQueueLimit packetQueueLimit} (e.g. <code>16</code>).
     */
    private boolean shouldRetransmit = true;
    /**
     * Positive number indicating how many <code>unacked</code> or <code>unordered</code> packaged user-datas should be
     * queued maximally until they can be resolved. Defaults to {@link ProtocolConfig#MAX_PACKET_QUEUE_LIMIT}.
     */
    private int packetQueueLimit = MAX_PACKET_QUEUE_LIMIT;
    /**
     * Positive number indicating the maximum <code>id offset</code> of unresolved <code>unacked</code> or <code>unacked</code>
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


    private int K = 2;
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
     *
     * @param packetQueueLimit the packet queue limit
     */
    public void setPacketQueueLimit(int packetQueueLimit) {
        packetQueueLimit = packetQueueLimit < MAX_PACKET_QUEUE_LIMIT ? packetQueueLimit : MAX_PACKET_QUEUE_LIMIT;
        packetQueueLimit = packetQueueLimit >= 0 ? packetQueueLimit : 0;
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
     *
     * @param packetOffsetLimit the packet offset limit
     */
    public void setPacketOffsetLimit(int packetOffsetLimit) {
        packetOffsetLimit = packetOffsetLimit < MAX_PACKET_OFFSET_LIMIT ? packetOffsetLimit : MAX_PACKET_OFFSET_LIMIT;
        packetOffsetLimit = packetOffsetLimit >= 0 ? packetOffsetLimit : 0;
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
     *
     * @param packetRetransmitLimit the packet retransmit limit
     */
    public void setPacketRetransmitLimit(int packetRetransmitLimit) {
        packetRetransmitLimit = packetRetransmitLimit < MAX_PACKET_QUEUE_LIMIT ? packetRetransmitLimit : MAX_PACKET_QUEUE_LIMIT;
        packetRetransmitLimit = packetRetransmitLimit >= 0 ? packetRetransmitLimit : 0;
        this.packetRetransmitLimit = packetRetransmitLimit;
    }

    /**
     * Gets a boolean indicating whether the protocol should retransmit data. Defaults to <code>true</code>.
     * If this setting is changed to <code>false</code>, more "aggressive" values should be set for
     * {@link ProtocolConfig#packetOffsetLimit packetOffsetLimit} (e.g. <code>16</code>) and
     * {@link ProtocolConfig#packetQueueLimit packetQueueLimit} (e.g. <code>16</code>).
     *
     * @return boolean whether to retransmit or not
     */
    public boolean shouldRetransmit() {
        return shouldRetransmit;
    }

    /**
     * Sets a boolean indicating whether the protocol should retransmit data. Defaults to <code>true</code>.
     * If this setting is changed to <code>false</code>, more "aggressive" values should be set for
     * {@link ProtocolConfig#packetOffsetLimit packetOffsetLimit} (e.g. <code>16</code>) and
     * {@link ProtocolConfig#packetQueueLimit packetQueueLimit} (e.g. <code>16</code>).
     *
     * @param shouldRetransmit boolean whether to retranmit or not
     */
    public void setShouldRetransmit(boolean shouldRetransmit) {
        this.shouldRetransmit = shouldRetransmit;
    }

    public int getK() {
        return K;
    }

    public int getG() {
        return G;
    }
}
