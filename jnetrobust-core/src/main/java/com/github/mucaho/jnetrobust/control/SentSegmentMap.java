/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SentSegmentComparator;

import java.util.NavigableSet;

public class SentSegmentMap<T> extends AbstractSegmentMap<T> {
    public SentSegmentMap() {
        super(IdComparator.instance, SentSegmentComparator.instance);
    }

    @Override
    void putAll(Segment<T> segment) {
        putAll(segment.getTransmissionIds(), segment);
    }

    @Override
    void putAll(NavigableSet<Short> transmissionIds, Segment<T> segment) {
        Short nextKey = transmissionIds.first();
        while (nextKey != null) {
            put(nextKey, segment);
            nextKey = transmissionIds.higher(nextKey);
        }
    }

    @Override
    Segment<T> put(Short transmissionId, Segment<T> segment) {
        // remove from valueMap, then possibly re-add after modifying transmissionIds
        valueMap.remove(segment);
        segment.addTransmissionId(transmissionId);
        if (!segment.getTransmissionIds().isEmpty())
            valueMap.put(segment, segment.getTransmissionIds());

        Segment<T> replacedSegment = keyMap.put(transmissionId, segment);

        if (replacedSegment != null && replacedSegment != segment) {
            // remove from valueMap, then possibly re-add after modifying transmissionIds
            valueMap.remove(replacedSegment);
            replacedSegment.removeTransmissionId(transmissionId);
            if (!replacedSegment.getTransmissionIds().isEmpty())
                valueMap.put(replacedSegment, replacedSegment.getTransmissionIds());
        }

        return replacedSegment;
    }

    @Override
    Segment<T> put(Segment<T> segment) {
        if (!segment.getTransmissionIds().isEmpty())
            return put(segment.getLastTransmissionId(), segment);
        else
            return null;
    }

    @Override
    Segment<T> removeAll(Short transmissionId) {
        return removeAll(getValue(transmissionId));
    }

    @Override
    Segment<T> removeAll(Segment<T> segment) {
        if (segment != null)
            removeAll(segment.getTransmissionIds());

        return segment;
    }

    @Override
    void removeAll(NavigableSet<Short> transmissionIds) {
        Short nextKey = transmissionIds.first();
        while (nextKey != null) {
            remove(nextKey);
            nextKey = transmissionIds.higher(nextKey);
        }
    }

    @Override
    Segment<T> remove(Short transmissionId) {
        Segment<T> segment = keyMap.remove(transmissionId);
        if (segment != null) {
            // remove from valueMap, then possibly re-add after modifying transmissionIds
            valueMap.remove(segment);
            segment.removeTransmissionId(transmissionId);
            if (!segment.getTransmissionIds().isEmpty())
                valueMap.put(segment, segment.getTransmissionIds());
        }

        return segment;
    }

    @Override
    void clear(boolean thourough) {
        if (thourough) {
            NavigableSet<Segment<T>> segments = valueMap.navigableKeySet();
            Segment<T> segment = segments.isEmpty() ? null : segments.first();
            while (segment != null) {
                segment.clearTransmissionIds();

                segment = segments.higher(segment);
            }
        }

        super.clear(thourough);
    }
}
