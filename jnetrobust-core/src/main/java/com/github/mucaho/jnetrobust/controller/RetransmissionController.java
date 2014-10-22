/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.control.ResponseControl;

import java.util.Collection;

public class RetransmissionController<T> extends Controller<T> {
    protected ResponseControl<T> responseHandler;

    public RetransmissionController(ProtocolConfig<T> config) {
        super(config);
        responseHandler = new ResponseControl<T>(pendingMapHandler.getMap().values());
    }

    @Override
    public void send(Packet<T> packet, Metadata<T> metadata) {
        // Update last modified time
        responseHandler.resetPendingTime(metadata);

        super.send(packet, metadata);
    }

    public Collection<Metadata<T>> retransmit() {
        // Update outdated not acked packets
        Collection<Metadata<T>> retransmits = responseHandler.updatePendingTime(rttHandler.getRTO());
        if (!retransmits.isEmpty()) {
            rttHandler.backoff();
        }
        return retransmits;
    }
}
