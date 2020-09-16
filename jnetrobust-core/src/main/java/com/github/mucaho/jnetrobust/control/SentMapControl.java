/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.FastLog;

import static com.github.mucaho.jnetrobust.util.BitConstants.LSB;
import static com.github.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class SentMapControl<T> extends AbstractMapControl<T> {
    public interface TransmissionSuccessListener<T> {
        void handleAckedData(short dataId, T ackedData);
        void handleUnackedData(short dataId, T unackedData);
    }

    private TransmissionSuccessListener<T> listener;

    public SentMapControl(TransmissionSuccessListener<T> listener, int maxEntries, int maxEntryOffset,
                          int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
    }

    @Override
    protected AbstractSegmentMap<T> createMap() {
        return new SentSegmentMap<T>();
    }

    public void addToSent(Short transmissionId, Segment<T> segment) {
        // add to pending map
        dataMap.put(transmissionId, segment);

        // discard old entries in pending map
        discardEntries();
    }

    public void removeFromSent(Short transmissionId, long precedingTransmissionIds) {
        // remove multiple (oldest until newest) from pending map
        removeFromSentOnBits(transmissionId, precedingTransmissionIds);

        // remove newest from pending map
        notifyAcked(dataMap.removeAll(transmissionId), true);
    }

    private void removeFromSentOnBits(short transmissionId, long precedingTransmissionIds) {
        short precedingTransmissionId;
        int msbIndex;
        while (precedingTransmissionIds != 0) {
            msbIndex = FastLog.log2(precedingTransmissionIds);
            precedingTransmissionId = (short) (transmissionId - msbIndex - OFFSET);
            notifyAcked(dataMap.removeAll(precedingTransmissionId), false);
            precedingTransmissionIds &= ~(LSB << msbIndex);
        }

    }

    @Override
    protected void discardEntry(Short key) {
        notifyNotAcked(dataMap.removeAll(key));
    }

    @Override
    protected void discardEntry(Segment<T> segment) {
        notifyNotAcked(dataMap.removeAll(segment));
    }

    @Override
    protected void discardEntryKey(Short key) {
        Segment<T> shrankSegment = dataMap.remove(key);
        if (shrankSegment != null && shrankSegment.getTransmissionIds().isEmpty())
            notifyNotAcked(shrankSegment);
    }

    protected void notifyNotAcked(Segment<T> unackedSegment) {
        if (unackedSegment != null)
            listener.handleUnackedData(unackedSegment.getDataId(), unackedSegment.getData());
    }

    protected void notifyAcked(Segment<T> ackedSegment, boolean directlyAcked) {
        if (ackedSegment != null)
            listener.handleAckedData(ackedSegment.getDataId(), ackedSegment.getData());
    }
}
