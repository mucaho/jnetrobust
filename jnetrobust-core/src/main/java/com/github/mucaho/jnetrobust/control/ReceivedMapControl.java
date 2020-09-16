/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;

public class ReceivedMapControl<T> extends AbstractMapControl<T> {
    public interface TransmissionOrderListener<T> {
        void handleOrderedData(short dataId, T orderedData);
        void handleUnorderedData(short dataId, T unorderedData);
    }

    private final TransmissionOrderListener<T> listener;
    private short nextDataId;

    public ReceivedMapControl(short initialDataId, TransmissionOrderListener<T> listener, int maxEntries, int maxEntryOffset,
                              int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
        this.nextDataId = (short) (initialDataId + 1);
    }

    @Override
    protected AbstractSegmentMap<T> createMap() {
        return new ReceivedSegmentMap<T>() {
            Segment<T> put(Segment<T> segment) {
                if (IdComparator.instance.compare(segment.getDataId(), nextDataId) >= 0) {
                    return super.put(segment);
                }
                return null;
            }
        };
    }

    public void addToReceived(Segment<T> segment) {
        // add original to received map
        dataMap.put(segment);

        // discard old entries in received map
        discardEntries();

        // remove multiple from map -> least, consecutive, ordered elements
        removeTail();
    }

    private void removeTail() {
        Short key = dataMap.firstKey();
        while (key != null && key == nextDataId) {
            notifyOrdered(dataMap.removeAll(key));

            key = dataMap.higherKey(key);
            nextDataId++;
        }
    }

    @Override
    protected void discardEntry(Short key) {
        nextDataId = IdComparator.instance.compare((short) (key + 1), nextDataId) > 0 ?
                (short) (key + 1) : nextDataId;
        notifyUnordered(dataMap.removeAll(key));
    }

    @Override
    protected void discardEntry(Segment<T> segment) {
        discardEntry(segment.getDataId());
    }

    @Override
    protected void discardEntryKey(Short key) {
        discardEntry(key);
    }

    protected void notifyUnordered(Segment<T> unorderedSegment) {
        if (unorderedSegment != null)
            listener.handleUnorderedData(unorderedSegment.getDataId(), unorderedSegment.getData());
    }

    protected void notifyOrdered(Segment<T> orderedSegment) {
        if (orderedSegment != null)
            listener.handleOrderedData(orderedSegment.getDataId(), orderedSegment.getData());
    }
}
