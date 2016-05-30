/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.FastLog;

import static com.github.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class SentMapControl<T> extends AbstractMapControl<T> {
    public interface TransmissionSuccessListener<T> {
        void handleAckedData(short dataId, T ackedData);
        void handleUnackedData(short dataId, T unackedData);
    }

    private final TransmissionSuccessListener<T> listener;

    public SentMapControl(TransmissionSuccessListener<T> listener, int maxEntries, int maxEntryOffset,
                          int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
    }

    @Override
    protected AbstractMetadataMap<T> createMap() {
        return new SentMetadataMap<T>();
    }

    public void addToSent(short transmissionId, Metadata<T> metadata) {
        // add to pending map
        dataMap.put(transmissionId, metadata);

        // discard old entries in pending map
        discardEntries();
    }

    public void removeFromSent(short transmissionId, int precedingTransmissionIds) {
        // remove multiple (oldest until newest) from pending map
        removeFromSentOnBits(transmissionId, precedingTransmissionIds);

        // remove newest from pending map
        notifyAcked(dataMap.removeAll(transmissionId), true);
    }

    private void removeFromSentOnBits(short transmissionId, int precedingTransmissionIds) {
        short precedingTransmissionId;
        int msbIndex;
        while (precedingTransmissionIds != 0) {
            msbIndex = FastLog.log2(precedingTransmissionIds);
            precedingTransmissionId = (short) (transmissionId - msbIndex - OFFSET);
            notifyAcked(dataMap.removeAll(precedingTransmissionId), false);
            precedingTransmissionIds &= ~(0x1 << msbIndex);
        }

    }

    @Override
    protected void discardEntry(short key) {
        notifyNotAcked(dataMap.removeAll(key));
    }

    @Override
    protected void discardEntry(Metadata<T> metadata) {
        notifyNotAcked(dataMap.removeAll(metadata));
    }

    @Override
    protected void discardEntryKey(short key) {
        Metadata<T> shrankMetadata = dataMap.remove(key);
        if (shrankMetadata != null && shrankMetadata.getTransmissionIds().isEmpty())
            notifyNotAcked(shrankMetadata);
    }

    protected void notifyNotAcked(Metadata<T> unackedMetadata) {
        if (unackedMetadata != null)
            listener.handleUnackedData(unackedMetadata.getDataId(), unackedMetadata.getData());
    }

    protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
        if (ackedMetadata != null)
            listener.handleAckedData(ackedMetadata.getDataId(), ackedMetadata.getData());
    }
}
