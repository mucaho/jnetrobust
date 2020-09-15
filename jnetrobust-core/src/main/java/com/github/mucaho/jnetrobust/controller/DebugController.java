/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.ProtocolListener;
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.Logger;

import java.util.List;

public class DebugController<T> extends Controller<T> {
    private final Logger logger;

    public DebugController(ProtocolListener<T> listener, ProtocolConfig config, Logger logger) {
        super(listener, config);
        this.logger = logger;
    }

    @Override
    public Metadata<T> produce(T data) {
        Metadata<T> out = super.produce(data);
        T value = out.getData();

        logger.log(Logger.LoggingEvent.SEND.toString(), String.valueOf(out.getDataId()),
                value != null ? value.toString() : "null");

        return out;
    }

    @Override
    public T consume(Metadata<T> metadata) {
        T value = metadata.getData();
        logger.log(Logger.LoggingEvent.RECEIVE.toString(), String.valueOf(metadata.getDataId()),
                value != null ? value.toString() : "null");

        return super.consume(metadata);
    }

    @Override
    public List<Metadata<T>> retransmit() {
        List<Metadata<T>> retransmits = super.retransmit();
        for (int i = 0, l = retransmits.size(); i < l; ++i) {
            Metadata<T> retransmit = retransmits.get(i);
            logger.log(Logger.LoggingEvent.SEND_RETRANSMISSION.toString(), String.valueOf(retransmit.getDataId()),
                    retransmit.getData() != null ? retransmit.getData().toString() : "null");
        }

        return retransmits;
    }
}
