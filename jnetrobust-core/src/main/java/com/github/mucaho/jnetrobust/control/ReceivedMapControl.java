/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SystemClock;

import java.nio.ByteBuffer;

public class ReceivedMapControl extends AbstractMapControl {
    public interface TransmissionOrderListener {
        void handleOrderedData(short dataId, ByteBuffer orderedData);
        void handleUnorderedData(short dataId, ByteBuffer unorderedData);
    }

    private final TransmissionOrderListener listener;
    private short nextDataId;

    public ReceivedMapControl(short initialDataId, TransmissionOrderListener listener, int maxEntries, int maxEntryOffset,
                              int maxEntryOccurrences, long maxEntryTimeout, SystemClock systemClock) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout, systemClock);
        this.listener = listener;
        this.nextDataId = (short) (initialDataId + 1);
    }

    @Override
    protected AbstractSegmentMap createMap() {
        return new ReceivedSegmentMap() {
            Segment put(Segment segment) {
                if (IdComparator.instance.compare(segment.getDataId(), nextDataId) >= 0) {
                    return super.put(segment);
                }
                return null;
            }
        };
    }

    public void addToReceived(Segment segment) {
        // add original to received map
        dataMap.put(segment);
    }

    public void removeFromTail() {
        // remove multiple from map -> least, consecutive, ordered elements
        Short key = dataMap.firstKey();
        while (key != null && key == nextDataId) {
            notifyOrdered(key, dataMap.removeAll(key));

            key = dataMap.higherKey(key);
            nextDataId++;
        }
    }

    @Override
    protected void discardEntry(Short key) {
        nextDataId = IdComparator.instance.compare((short) (key + 1), nextDataId) > 0 ?
                (short) (key + 1) : nextDataId;
        notifyUnordered(key, dataMap.removeAll(key));
    }

    @Override
    protected void discardEntry(Segment segment) {
        discardEntry(segment.getDataId());
    }

    @Override
    protected void discardEntryKey(Short key) {
        discardEntry(key);
    }

    protected void notifyUnordered(Short dataId, Segment unorderedSegment) {
        if (unorderedSegment != null) {
            listener.handleUnorderedData(unorderedSegment.getDataId(), unorderedSegment.getData());
            if (unorderedSegment.getData() != null) unorderedSegment.getData().rewind();
        }
    }

    protected void notifyOrdered(Short dataId, Segment orderedSegment) {
        if (orderedSegment != null) {
            listener.handleOrderedData(orderedSegment.getDataId(), orderedSegment.getData());
            if (orderedSegment.getData() != null) orderedSegment.getData().rewind();
        }
    }
}
