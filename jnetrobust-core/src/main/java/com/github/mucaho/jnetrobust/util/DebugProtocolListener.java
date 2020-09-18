/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.Logger;
import com.github.mucaho.jnetrobust.ProtocolListener;

import java.nio.ByteBuffer;

public class DebugProtocolListener extends ProtocolListener {

    private final Logger logger;
    private final ProtocolListener delegate;

    public DebugProtocolListener(ProtocolListener delegate, Logger logger) {
        this.logger = logger;
        this.delegate = delegate;
    }

    @Override
    public void handleOrderedData(short dataId, ByteBuffer orderedData) {
        logger.log(Logger.LoggingEvent.ORDERED.toString(), dataId, orderedData);
        delegate.handleOrderedData(dataId, orderedData);
    }

    @Override
    public void handleUnorderedData(short dataId, ByteBuffer unorderedData) {
        logger.log(Logger.LoggingEvent.UNORDERED.toString(), dataId, unorderedData);
        delegate.handleUnorderedData(dataId, unorderedData);
    }

    @Override
    public void handleAckedData(short dataId, ByteBuffer ackedData) {
        logger.log(Logger.LoggingEvent.ACKED.toString(), dataId, ackedData);
        delegate.handleAckedData(dataId, ackedData);
    }

    @Override
    public void handleUnackedData(short dataId, ByteBuffer unackedData) {
        logger.log(Logger.LoggingEvent.NOTACKED.toString(), dataId, unackedData);
        delegate.handleUnackedData(dataId, unackedData);
    }

    @Override
    public void handleNewestData(short dataId, ByteBuffer newestData) {
        logger.log(Logger.LoggingEvent.NEWEST.toString(), dataId, newestData);
        delegate.handleNewestData(dataId, newestData);
    }

    @Override
    public Boolean shouldRetransmit(short dataId, ByteBuffer retransmitData) {
        logger.log(Logger.LoggingEvent.RETRANSMISSION.toString(), dataId, retransmitData);
        return delegate.shouldRetransmit(dataId, retransmitData);
    }
}
