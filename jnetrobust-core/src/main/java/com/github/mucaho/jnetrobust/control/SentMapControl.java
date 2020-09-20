/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.FastLog;
import com.github.mucaho.jnetrobust.util.SystemClock;

import java.nio.ByteBuffer;

import static com.github.mucaho.jnetrobust.util.BitConstants.LSB;
import static com.github.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class SentMapControl extends AbstractMapControl {
    public interface TransmissionSuccessListener {
        void handleAckedData(short dataId, ByteBuffer ackedData);
        void handleUnackedData(short dataId, ByteBuffer unackedData);
    }

    private TransmissionSuccessListener listener;

    public SentMapControl(TransmissionSuccessListener listener, int maxEntries, int maxEntryOffset,
                          int maxEntryOccurrences, long maxEntryTimeout, SystemClock systemClock) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout, systemClock);
        this.listener = listener;
    }

    @Override
    protected AbstractSegmentMap createMap() {
        return new SentSegmentMap();
    }

    public void addToSent(Short transmissionId, Segment segment) {
        // add to pending map
        dataMap.put(transmissionId, segment);
    }

    public void removeFromSent(Short transmissionId, long precedingTransmissionIds) {
        // remove multiple (oldest until newest) from pending map
        removeFromSentOnBits(transmissionId, precedingTransmissionIds);

        // remove newest from pending map
        notifyAcked(transmissionId, dataMap.removeAll(transmissionId), true);
    }

    private void removeFromSentOnBits(Short transmissionId, long precedingTransmissionIds) {
        Short precedingTransmissionId;
        int msbIndex;
        while (precedingTransmissionIds != 0) {
            msbIndex = FastLog.log2(precedingTransmissionIds);
            precedingTransmissionId = (short) (transmissionId - msbIndex - OFFSET);
            notifyAcked(precedingTransmissionId, dataMap.removeAll(precedingTransmissionId), false);
            precedingTransmissionIds &= ~(LSB << msbIndex);
        }

    }

    @Override
    protected void discardEntry(Short key) {
        notifyNotAcked(key, dataMap.removeAll(key));
    }

    @Override
    protected void discardEntry(Segment segment) {
        notifyNotAcked(segment.getLastTransmissionId(), dataMap.removeAll(segment));
    }

    @Override
    protected void discardEntryKey(Short key) {
        Segment shrankSegment = dataMap.remove(key);
        if (shrankSegment != null && shrankSegment.getTransmissionIds().isEmpty())
            notifyNotAcked(key, shrankSegment);
    }

    protected void notifyNotAcked(Short transmissionId, Segment unackedSegment) {
        if (unackedSegment != null) {
            listener.handleUnackedData(unackedSegment.getDataId(), unackedSegment.getData());
            if (unackedSegment.getData() != null) unackedSegment.getData().rewind();
        }
    }

    protected void notifyAcked(Short transmissionId, Segment ackedSegment, boolean directlyAcked) {
        if (ackedSegment != null) {
            listener.handleAckedData(ackedSegment.getDataId(), ackedSegment.getData());
            if (ackedSegment.getData() != null) ackedSegment.getData().rewind();
        }
    }
}
