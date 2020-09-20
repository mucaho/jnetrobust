/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SystemClock;
import com.github.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.*;


public abstract class AbstractMapControl {
    private final TimeoutHandler entryTimeoutHandler =
            new TimeoutHandler();

    protected final int maxEntries;
    protected final int maxEntryOffset;
    protected final int maxEntryOccurrences;
    protected final long maxEntryTimeout;
    protected final SystemClock systemClock;

    /*
     * [lastSeq]-...-[seq-32]-[seq-31]-...-[seq-1]-[seq]
     */
    protected AbstractSegmentMap dataMap;

    public AbstractMapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout,
                              SystemClock systemClock) {
        this.maxEntries = maxEntries;
        this.maxEntryOffset = maxEntryOffset;
        this.maxEntryOccurrences = maxEntryOccurrences;
        this.maxEntryTimeout = maxEntryTimeout;
        this.systemClock = systemClock;
        this.dataMap = createMap();
    }

    protected abstract AbstractSegmentMap createMap();

    public NavigableSet<Short> getKeys() {
        return dataMap.getKeys();
    }

    public NavigableSet<Segment> getValues() {
        return dataMap.getValues();
    }

    protected abstract void discardEntry(Short key);
    protected abstract void discardEntry(Segment segment);
    protected abstract void discardEntryKey(Short key);

    public void discardEntries() {
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
            List<Segment> timedOuts =
                    entryTimeoutHandler.filterTimedout(dataMap.getValues(), maxEntryTimeout, systemClock.getTimeNow());
            for (int i = 0, l = timedOuts.size(); i < l; ++i)
                discardEntry(timedOuts.get(i));
        }
    }

    private void discardTooManyDistinctEntryValues() {
        Segment segment = dataMap.firstValue();
        while (segment != null && dataMap.valueSize() > maxEntries) {
            discardEntry(segment);
            segment = dataMap.firstValue();
        }
    }

    private void discardEntriesWithTooManyEntryKeys() {
        if (maxEntryOccurrences > 0) {
            Segment segment = dataMap.firstValue();
            while (segment != null && dataMap.getKeys(segment).size() > maxEntryOccurrences) {
                discardEntry(segment);
                segment = dataMap.firstValue();
            }
        }
    }
}
