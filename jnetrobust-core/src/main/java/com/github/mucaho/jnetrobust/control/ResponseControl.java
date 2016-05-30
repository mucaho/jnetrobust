/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;

public class ResponseControl<T> {
    public interface RetransmissionListener<T> {
        Boolean shouldRetransmit(short dataId, T data);
    }
    private final RetransmissionListener<T> listener;

    private final TimeoutHandler<Metadata<T>> sentDataTimeoutHandler = new TimeoutHandler<Metadata<T>>();

    private final NavigableSet<Metadata<T>> sentMetadatas;

    private final boolean autoRetransmit;

    private final List<Metadata<T>> retransmissions = new ArrayList<Metadata<T>>();
    private final List<Metadata<T>> retransmissionsOut = Collections.unmodifiableList(retransmissions);

    public ResponseControl(NavigableSet<Metadata<T>> sentMetadatas, RetransmissionListener<T> listener, boolean autoRetransmit) {
        this.sentMetadatas = sentMetadatas;
        this.listener = listener;
        this.autoRetransmit = autoRetransmit;
    }

    public void resetPendingTime(Metadata<T> sentMetadatas) {
        sentMetadatas.updateTime();
    }

    public List<Metadata<T>> updatePendingTime(long maxWaitTime) {
        retransmissions.clear();

        List<Metadata<T>> potentialRetransmits = sentDataTimeoutHandler.filterTimedOut(sentMetadatas, maxWaitTime);
        for (int i = 0, l = potentialRetransmits.size(); i < l; ++i) {
            Boolean doIt = determineRetransmit(potentialRetransmits.get(i));
            if (Boolean.TRUE.equals(doIt) || (doIt == null && autoRetransmit))
                retransmissions.add(potentialRetransmits.get(i));
        }

        return retransmissionsOut;
    }

    protected Boolean determineRetransmit(Metadata<T> retransmit) {
        return listener.shouldRetransmit(retransmit.getDataId(), retransmit.getData());
    }
}
