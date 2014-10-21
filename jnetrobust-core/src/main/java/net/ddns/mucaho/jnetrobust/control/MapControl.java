/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.IdComparator;
import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;


public abstract class MapControl<T> {
    protected final static Comparator<Short> comparator = IdComparator.instance;

    private final TimeoutHandler<Metadata<T>> entryTimeoutHandler =
            new TimeoutHandler<Metadata<T>>();

    protected final int maxEntries;
    protected final int maxEntryOffset;
    protected final int maxEntryOccurrences;
    protected final long maxEntryTimeout;

    /*
     * [lastSeq]-...-[seq-32]-[seq-31]-...-[seq-1]-[seq]
     */
    protected MetadataMap<T> dataMap;

    public MapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout) {
        this.maxEntries = maxEntries;
        this.maxEntryOffset = maxEntryOffset;
        this.maxEntryOccurrences = maxEntryOccurrences;
        this.maxEntryTimeout = maxEntryTimeout;
        createMap();
    }

    protected void createMap() {
        dataMap = new MetadataMap<T>(comparator);
    }

    public NavigableMap<Short, Metadata<T>> getMap() {
        return dataMap.getMap();
    }

    protected abstract void discardEntry(short key);

    protected void discardEntries() {
        discardTooManyEntries();
        discardTooOldEntries();
        discardTimedoutEntries();
        discardEntryTooOften();
    }


    private void discardTooManyEntries() {
        while (dataMap.size() > maxEntries) {
            discardEntry(dataMap.firstKey());
        }
    }

    private void discardTooOldEntries() {
        if (!dataMap.isEmpty()) {
            short newestKey = dataMap.lastKey();
            short oldestKey = dataMap.firstKey();
            while (comparator.compare(newestKey, oldestKey) >= maxEntryOffset) {
                discardEntry(oldestKey);
                oldestKey = dataMap.firstKey();
            }
        }
    }

    private void discardTimedoutEntries() {
        if (maxEntryTimeout > 0) {
            Collection<Metadata<T>> timedOuts =
                    entryTimeoutHandler.filterTimedOut(dataMap.getMap().values(), maxEntryTimeout);
            for (Metadata<T> timedOut : timedOuts)
                discardEntry(timedOut.getFirstTransmissionId());
        }
    }

    private void discardEntryTooOften() {
        if (maxEntryOccurrences > 0) {
            Short key = dataMap.firstKey();
            while (key != null) {
                if (dataMap.get(key).getTransmissionIds().size() > maxEntryOccurrences)
                    discardEntry(key);
                key = dataMap.higherKey(key);
            }
        }
    }
}
