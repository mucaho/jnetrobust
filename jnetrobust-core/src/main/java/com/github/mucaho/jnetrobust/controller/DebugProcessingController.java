/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.ProtocolListener;
import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.Logger;

import java.nio.ByteBuffer;
import java.util.List;

public class DebugProcessingController extends ProcessingController {
    private final Logger logger;

    public DebugProcessingController(ProtocolListener listener, ProtocolConfig config, Logger logger) {
        super(listener, config);
        this.logger = logger;
    }

    @Override
    public Segment produce(ByteBuffer data) {
        Segment out = super.produce(data);

        logger.log(Logger.LoggingEvent.SEND.toString(), out.getDataId(), out.getData());

        return out;
    }

    @Override
    public ByteBuffer consume(Segment segment) {
        logger.log(Logger.LoggingEvent.RECEIVE.toString(), segment.getDataId(), segment.getData());

        return super.consume(segment);
    }

    @Override
    public List<Segment> retransmit() {
        List<Segment> retransmits = super.retransmit();
        for (int i = 0, l = retransmits.size(); i < l; ++i) {
            Segment retransmit = retransmits.get(i);
            logger.log(Logger.LoggingEvent.SEND_RETRANSMISSION.toString(), retransmit.getDataId(), retransmit.getData());
        }

        return retransmits;
    }
}
