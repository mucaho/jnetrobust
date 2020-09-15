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
    private final TimeoutHandler<Metadata<T>> entryTimeoutHandler =
            new TimeoutHandler<Metadata<T>>();

    protected final int maxEntries;
    protected final int maxEntryOffset;
    protected final int maxEntryOccurrences;
    protected final long maxEntryTimeout;

    /*
     * [lastSeq]-...-[seq-32]-[seq-31]-...-[seq-1]-[seq]
     */
    protected AbstractMetadataMap<T> dataMap;

    public AbstractMapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout) {
        this.maxEntries = maxEntries;
        this.maxEntryOffset = maxEntryOffset;
        this.maxEntryOccurrences = maxEntryOccurrences;
        this.maxEntryTimeout = maxEntryTimeout;
        this.dataMap = createMap();
    }

    protected abstract AbstractMetadataMap<T> createMap();

    public NavigableSet<Short> getKeys() {
        return dataMap.getKeys();
    }

    public NavigableSet<Metadata<T>> getValues() {
        return dataMap.getValues();
    }

    protected abstract void discardEntry(Short key);
    protected abstract void discardEntry(Metadata<T> metadata);
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
            List<Metadata<T>> timedOuts =
                    entryTimeoutHandler.filterTimedOut(dataMap.getValues(), maxEntryTimeout);
            for (int i = 0, l = timedOuts.size(); i < l; ++i)
                discardEntry(timedOuts.get(i));
        }
    }

    private void discardTooManyDistinctEntryValues() {
        Metadata<T> metadata = dataMap.firstValue();
        while (metadata != null && dataMap.valueSize() > maxEntries) {
            discardEntry(metadata);
            metadata = dataMap.firstValue();
        }
    }

    private void discardEntriesWithTooManyEntryKeys() {
        if (maxEntryOccurrences > 0) {
            Metadata<T> metadata = dataMap.firstValue();
            while (metadata != null && dataMap.getKeys(metadata).size() > maxEntryOccurrences) {
                discardEntry(metadata);
                metadata = dataMap.firstValue();
            }
        }
    }
}
