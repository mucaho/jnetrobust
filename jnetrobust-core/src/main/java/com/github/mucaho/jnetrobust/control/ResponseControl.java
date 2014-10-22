/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;

public class ResponseControl<T> {
    private final TimeoutHandler<Metadata<T>> pendingDataTimeoutHandler =
            new TimeoutHandler<Metadata<T>>();

    private final Collection<Metadata<T>> pendingDatas;

    public ResponseControl(Collection<Metadata<T>> pendingMetadatas) {
        this.pendingDatas = pendingMetadatas;
    }

    public void resetPendingTime(Metadata<T> pendingMetadata) {
        pendingMetadata.updateTime();
    }

    public Collection<Metadata<T>> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
    }
}
