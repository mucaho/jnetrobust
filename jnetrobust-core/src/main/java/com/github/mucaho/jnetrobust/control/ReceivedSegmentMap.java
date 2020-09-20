/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SegmentDataIdComparator;

import java.util.NavigableSet;

public class ReceivedSegmentMap extends AbstractSegmentMap {

    public ReceivedSegmentMap() {
        super(IdComparator.instance, SegmentDataIdComparator.instance);
    }

    @Override
    void putAll(Segment segment) {
        putAll(segment.getDataIds(), segment);
    }

    @Override
    void putAll(NavigableSet<Short> dataIds, Segment segment) {
        Short nextKey = dataIds.first();
        while (nextKey != null) {
            put(nextKey, segment);
            nextKey = dataIds.higher(nextKey);
        }
    }

    @Override
    Segment put(Short dataId, Segment segment) {
        valueMap.put(segment, segment.getDataIds());
        return keyMap.put(dataId, segment);
    }

    @Override
    Segment put(Segment segment) {
        return put(segment.getDataId(), segment);
    }

    @Override
    Segment removeAll(Short dataId) {
        return removeAll(getValue(dataId));
    }

    @Override
    Segment removeAll(Segment segment) {
        if (segment != null)
            removeAll(segment.getDataIds());

        return segment;
    }

    @Override
    void removeAll(NavigableSet<Short> dataIds) {
        Short nextKey = dataIds.first();
        while (nextKey != null) {
            remove(nextKey);
            nextKey = dataIds.higher(nextKey);
        }
    }

    @Override
    Segment remove(Short dataId) {
        Segment removedSegment = keyMap.remove(dataId);
        if (removedSegment != null)
            valueMap.remove(removedSegment);
        return removedSegment;
    }
}
