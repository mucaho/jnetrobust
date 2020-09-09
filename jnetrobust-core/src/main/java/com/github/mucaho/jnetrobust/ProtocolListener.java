/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.PendingMapControl.TransmissionSuccessListener;
import com.github.mucaho.jnetrobust.control.ReceivedMapControl.TransmissionOrderListener;

/**
 * The listener which will be notified about specific {@link Protocol protocol} events.
 * @param <T> the user data type
 */
public class ProtocolListener<T> implements TransmissionSuccessListener<T>, TransmissionOrderListener<T> {

    /**
     * This protocol instance received data in the same order it was sent from another protocol instance.
     * @param dataId        the <code>id</code> of the data
     * @param orderedData   the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleOrderedData(short dataId, T orderedData) {
    }

    /**
     * This protocol instance was unable to receive data in the same order it was sent from another protocol instance.
     * It skipped over a gap of one or more unreceived datas and reestablished order with the first <code>unorderedData</code> after the gap.
     * @param dataId        the <code>id</code> of the data
     * @param unorderedData the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleUnorderedData(short dataId, T unorderedData) {
    }

    /**
     * This protocol instance was informed about the receipt of data at another protocol instance.
     * @param dataId    the <code>id</code> of the data
     * @param ackedData the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleAckedData(short dataId, T ackedData) {
    }

    /**
     * This protocol instance was not informed about the receipt of data at another protocol instance.
     * The data may or may not be received at the other protocol instance.
     * @param dataId        the <code>id</code> of the data
     * @param unackedData   the actual user-data; the supplied user-data should not be modified by the user / application as it's used internally later on
     */
    @Override
    public void handleUnackedData(short dataId, T unackedData) {
    }
}
