/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.*;


public abstract class AbstractMapControl<T> {
    private final TimeoutHandler<Segment<T>> entryTimeoutHandler =
            new TimeoutHandler<Segment<T>>();

    protected final int maxEntries;
    protected final int maxEntryOffset;
    protected final int maxEntryOccurrences;
    protected final long maxEntryTimeout;

    /*
     * [lastSeq]-...-[seq-32]-[seq-31]-...-[seq-1]-[seq]
     */
    protected AbstractSegmentMap<T> dataMap;

    public AbstractMapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout) {
        this.maxEntries = maxEntries;
        this.maxEntryOffset = maxEntryOffset;
        this.maxEntryOccurrences = maxEntryOccurrences;
        this.maxEntryTimeout = maxEntryTimeout;
        this.dataMap = createMap();
    }

    protected abstract AbstractSegmentMap<T> createMap();

    public NavigableSet<Short> getKeys() {
        return dataMap.getKeys();
    }

    public NavigableSet<Segment<T>> getValues() {
        return dataMap.getValues();
    }

    protected abstract void discardEntry(Short key);
    protected abstract void discardEntry(Segment<T> segment);
    protected abstract void discardEntryKey(Short key);

    protected void discardEntries() {
        discardTooOldEntryKeys();

        discardTimedoutEntries();
        discardTooManyDistinctEntryValues();
        discardEntriesWithTooManyEntryKeys();
    }

    private void discardTooOldEntryKeys() {
        if (!dataMap.isEmpty()) {
            Short newestKey = dataMap.lastKey();
            Short oldestKey = dataMap.firstKey();
            while (IdComparator.instance.compare(newestKey, oldestKey) >= maxEntryOffset) {
                discardEntryKey(oldestKey);
                oldestKey = dataMap.firstKey();
            }
        }
    }

    private void discardTimedoutEntries() {
        if (maxEntryTimeout > 0) {
            List<Segment<T>> timedOuts =
                    entryTimeoutHandler.filterTimedOut(dataMap.getValues(), maxEntryTimeout);
            for (int i = 0, l = timedOuts.size(); i < l; ++i)
                discardEntry(timedOuts.get(i));
        }
    }

    private void discardTooManyDistinctEntryValues() {
        Segment<T> segment = dataMap.firstValue();
        while (segment != null && dataMap.valueSize() > maxEntries) {
            discardEntry(segment);
            segment = dataMap.firstValue();
        }
    }

    private void discardEntriesWithTooManyEntryKeys() {
        if (maxEntryOccurrences > 0) {
            Segment<T> segment = dataMap.firstValue();
            while (segment != null && dataMap.getKeys(segment).size() > maxEntryOccurrences) {
                discardEntry(segment);
                segment = dataMap.firstValue();
            }
        }
    }
}
