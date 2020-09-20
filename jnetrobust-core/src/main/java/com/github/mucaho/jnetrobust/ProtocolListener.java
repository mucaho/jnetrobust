/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.NewestReceivedControl.NewestReceivedListener;
import com.github.mucaho.jnetrobust.control.SentMapControl.TransmissionSuccessListener;
import com.github.mucaho.jnetrobust.control.ReceivedMapControl.TransmissionOrderListener;
import com.github.mucaho.jnetrobust.control.RetransmissionControl.RetransmissionListener;

import java.nio.ByteBuffer;

/**
 * The listener which will be notified about specific {@link Protocol protocol} events.
 */
public class ProtocolListener implements TransmissionSuccessListener,
        TransmissionOrderListener, RetransmissionListener, NewestReceivedListener {

    /**
     * This protocol instance received data in the same order it was sent from another protocol instance.
     * @param dataId        the <code>id</code> of the data
     * @param orderedData   the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleOrderedData(short dataId, ByteBuffer orderedData) {
    }

    /**
     * This protocol instance was unable to receive data in the same order it was sent from another protocol instance.
     * It skipped over a gap of one or more unreceived datas and reestablished order with the first <code>unorderedData</code> after the gap.
     * @param dataId        the <code>id</code> of the data
     * @param unorderedData the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleUnorderedData(short dataId, ByteBuffer unorderedData) {
    }

    /**
     * This protocol instance was informed about the receipt of data at another protocol instance.
     * @param dataId    the <code>id</code> of the data
     * @param ackedData the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleAckedData(short dataId, ByteBuffer ackedData) {
    }

    /**
     * This protocol instance was not informed about the receipt of data at another protocol instance.
     * The data may or may not be received at the other protocol instance.
     * @param dataId        the <code>id</code> of the data
     * @param unackedData   the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleUnackedData(short dataId, ByteBuffer unackedData) {
    }

    /**
     * This protocol instance received the newest (latest / most recent) data from another protocol instance.
     * @param dataId        the <code>id</code> of the data
     * @param newestData    the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleNewestData(short dataId, ByteBuffer newestData) {
    }

    /**
     * This protocol instance has data that needs to be retransmitted.
     * The user can decide whether the protocol should retransmit the data or not on a case-by-case basis,
     * or let the protocol decide in general based on the {@link ProtocolConfig#getAutoRetransmitMode() AutoRetransmitMode}.
     * <br />
     * Note that by disabling automatic retransmission or purposefully denying retransmission in some cases, the total
     * ordering of data on the receiver end can no longer be guaranteed.
     * <br />
     * The user is still free to to retransmit the data, or some cloned and updated version of the data, later on using
     * the {@link Protocol#send(ByteBuffer) protocol's send} method.
     * <br /><br />
     * The following table summarizes whether the retransmission will occur based on the
     * {@link ProtocolConfig#getAutoRetransmitMode() AutoRetransmitMode setting} and
     * the user supplied {@code return Boolean value}.
     * <pre>
     *      +---------------------------------+--------+--------+-------+
     *      |            \ autoRetransmitMode | ALWAYS | NEWEST | NEVER |
     *      |             \                   |        |        |       |
     *      | return value \                  |        |        |       |
     *      +---------------------------------+--------+--------+-------+
     *      |     null                        |    Y   |   Y/N* |   N   |
     *      +---------------------------------+--------+--------+-------+
     *      |     true                        |    Y   |    Y   |   Y   |
     *      +---------------------------------+--------+--------+-------+
     *      |     false                       |    N   |    N   |   N   |
     *      +---------------------------------+--------+--------+-------+
     * </pre>
     * * In case of <code>NEWEST</code> the data is only automatically retransmitted if it was the most recent user-data
     * encountered so far.
     *
     * @param dataId            the <code>id</code> of the data
     * @param retransmitData    the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     * @return          a <code>null, false or true Boolean</code>, indicating the whether the data should be retransmitted
     */
    @Override
    public Boolean shouldRetransmit(short dataId, ByteBuffer retransmitData) {
        return null;
    }
}
