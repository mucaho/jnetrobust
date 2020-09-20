/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SegmentLastTransmissionIdComparator;
import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.nio.ByteBuffer;
import java.util.*;

public class RetransmissionControl {
    public interface RetransmissionListener {
        Boolean shouldRetransmit(short dataId, ByteBuffer retransmitData);
    }
    private final RetransmissionListener listener;

    private static final int FAST_RETRANSMIT_NDUPACK = 2;

    private final TimeoutHandler timeoutHandler = new TimeoutHandler();

    private final NavigableSet<Segment> sentSegments;
    private final NavigableSet<Segment> ackedSegments;

    private final ProtocolConfig.AutoRetransmitMode autoRetransmitMode;

    private final List<Segment> retransmissions = new ArrayList<Segment>();
    private final List<Segment> retransmissionsOut = Collections.unmodifiableList(retransmissions);

    public RetransmissionControl(NavigableSet<Segment> sentSegments,
                                 NavigableSet<Segment> ackedSegments,
                                 RetransmissionListener listener,
                                 ProtocolConfig.AutoRetransmitMode autoRetransmitMode) {
        this.sentSegments = sentSegments;
        this.ackedSegments = ackedSegments;
        this.listener = listener;
        this.autoRetransmitMode = autoRetransmitMode;
    }

    public void updateSentTime(Segment sentSegment, long timeNow) {
        sentSegment.setNewestSentTime(timeNow);
    }

    public void setAcknowledgedTime(Segment ackedSegment, long timeNow) {
        ackedSegment.setAckedTime(timeNow);
    }

    public List<Segment> getTimedoutRetransmits(long timeout, short newestDataId, long timeNow) {
        retransmissions.clear();

        List<Segment> potentialRetransmits = timeoutHandler.filterTimedout(sentSegments, timeout, timeNow);
        determineRetransmits(potentialRetransmits, newestDataId, retransmissions);

        return retransmissionsOut;
    }

    private final NavigableSet<Segment> allSegments = new TreeSet<Segment>(SegmentLastTransmissionIdComparator.instance);

    public List<Segment> getFastRetransmits(long relativeTimeout, short newestDataId) {
        retransmissions.clear();

        allSegments.clear();
        allSegments.addAll(sentSegments);
        allSegments.addAll(ackedSegments);
        List<Segment> fastRetransmits = timeoutHandler.filterLostInBetween(
                allSegments, relativeTimeout, FAST_RETRANSMIT_NDUPACK + 1);
        determineRetransmits(fastRetransmits, newestDataId, retransmissions);

        return retransmissionsOut;
    }

    private void determineRetransmits(List<Segment> in, short newestDataId, List<Segment> out) {
        for (int i = 0, l = in.size(); i < l; ++i) {
            Segment potentialRetransmit = in.get(i);

            Boolean userOk = determineRetransmit(potentialRetransmit);
            boolean doIt = Boolean.TRUE.equals(userOk);
            doIt = doIt || userOk == null
                    && autoRetransmitMode == ProtocolConfig.AutoRetransmitMode.ALWAYS;
            doIt = doIt || userOk == null
                    && autoRetransmitMode == ProtocolConfig.AutoRetransmitMode.NEWEST
                    && IdComparator.instance.equals(potentialRetransmit.getDataId(), newestDataId);
            if (doIt)
                out.add(potentialRetransmit);
        }
    }

    protected Boolean determineRetransmit(Segment retransmit) {
        Boolean shouldRetransmit = listener.shouldRetransmit(retransmit.getDataId(), retransmit.getData());
        if (retransmit.getData() != null) retransmit.getData().rewind();
        return shouldRetransmit;
    }
}
