/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;

public class RetransmissionControl {
    public interface RetransmissionListener {
        Boolean shouldRetransmit(short dataId, ByteBuffer retransmitData);
    }
    private final RetransmissionListener listener;

    private final TimeoutHandler<Segment> sentDataTimeoutHandler = new TimeoutHandler<Segment>();

    private final NavigableSet<Segment> sentSegments;

    private final ProtocolConfig.AutoRetransmitMode autoRetransmitMode;

    private final List<Segment> retransmissions = new ArrayList<Segment>();
    private final List<Segment> retransmissionsOut = Collections.unmodifiableList(retransmissions);

    public RetransmissionControl(NavigableSet<Segment> sentSegments,
                                 RetransmissionListener listener,
                                 ProtocolConfig.AutoRetransmitMode autoRetransmitMode) {
        this.sentSegments = sentSegments;
        this.listener = listener;
        this.autoRetransmitMode = autoRetransmitMode;
    }

    public void resetPendingTime(Segment sentSegments) {
        sentSegments.updateTime();
    }

    public List<Segment> updatePendingTime(long maxWaitTime, short newestDataId) {
        retransmissions.clear();

        List<Segment> potentialRetransmits = sentDataTimeoutHandler.filterTimedOut(sentSegments, maxWaitTime);
        for (int i = 0, l = potentialRetransmits.size(); i < l; ++i) {
            Segment potentialRetransmit = potentialRetransmits.get(i);

            Boolean userOk = determineRetransmit(potentialRetransmit);
            boolean doIt = Boolean.TRUE.equals(userOk);
            doIt = doIt || userOk == null
                    && autoRetransmitMode == ProtocolConfig.AutoRetransmitMode.ALWAYS;
            doIt = doIt || userOk == null
                    && autoRetransmitMode == ProtocolConfig.AutoRetransmitMode.NEWEST
                    && IdComparator.instance.equals(potentialRetransmit.getDataId(), newestDataId);
            if (doIt)
                retransmissions.add(potentialRetransmit);
        }

        return retransmissionsOut;
    }

    protected Boolean determineRetransmit(Segment retransmit) {
        Boolean shouldRetransmit = listener.shouldRetransmit(retransmit.getDataId(), retransmit.getData());
        if (retransmit.getData() != null) retransmit.getData().rewind();
        return shouldRetransmit;
    }
}
