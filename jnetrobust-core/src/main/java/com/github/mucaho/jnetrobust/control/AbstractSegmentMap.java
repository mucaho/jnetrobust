/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.CollectionUtils;
import com.github.mucaho.jnetrobust.util.EntryIterator;

import java.util.*;

public abstract class AbstractSegmentMap {
    protected final NavigableMap<Short, Segment> keyMap;
    private final NavigableMap<Short, Segment> keyMapOut;

    protected final NavigableMap<Segment, NavigableSet<Short>> valueMap;
    private final NavigableMap<Segment, NavigableSet<Short>> valueMapOut;

    private final EntryIterator<Short, Segment> entryIterator;

    public AbstractSegmentMap(Comparator<Short> idComparator, Comparator<Segment> segmentComparator) {
        this.keyMap = new TreeMap<Short, Segment>(idComparator);
        this.keyMapOut = CollectionUtils.unmodifiableNavigableMap(keyMap);

        this.valueMap = new TreeMap<Segment, NavigableSet<Short>>(segmentComparator);
        this.valueMapOut = CollectionUtils.unmodifiableNavigableMap(valueMap);

        this.entryIterator = new SegmentMapIterator(this);
    }

    public NavigableMap<Short, Segment> getKeyMap() {
        return keyMapOut;
    }

    public NavigableMap<Segment, NavigableSet<Short>> getValueMap() {
        return valueMapOut;
    }

    public NavigableSet<Short> getKeys() {
        return keyMapOut.navigableKeySet();
    }

    public NavigableSet<Segment> getValues() {
        return valueMapOut.navigableKeySet();
    }

    public Segment getValue(Short key) {
        return keyMapOut.get(key);
    }

    public NavigableSet<Short> getKeys(Segment segment) {
        return valueMapOut.get(segment);
    }

    abstract void putAll(Segment segment);

    abstract void putAll(NavigableSet<Short> keys, Segment segment);

    abstract Segment put(Short key, Segment segment);

    abstract Segment put(Segment segment);

    abstract Segment removeAll(Short key);

    abstract Segment removeAll(Segment segment);

    abstract void removeAll(NavigableSet<Short> keys);

    abstract Segment remove(Short key);

    public int keySize() {
        return keyMap.size();
    }

    public Short firstKey() {
        return keyMap.isEmpty() ? null : keyMap.firstKey();
    }

    public Short higherKey(Short key) {
        return keyMap.higherKey(key);
    }

    public Short lastKey() {
        return keyMap.isEmpty() ? null : keyMap.lastKey();
    }

    public Short lowerKey(Short key) {
        return keyMap.lowerKey(key);
    }

    public int valueSize() {
        return valueMap.size();
    }

    public Segment firstValue() {
        return valueMap.isEmpty() ? null : valueMap.firstKey();
    }

    public Segment higherValue(Segment segment) {
        return valueMap.higherKey(segment);
    }

    public Segment lastValue() {
        return valueMap.isEmpty() ? null : valueMap.lastKey();
    }

    public Segment lowerValue(Segment segment) {
        return valueMap.lowerKey(segment);
    }

    void clear() {
        clear(false);
    }

    void clear(boolean thourough) {
        keyMap.clear();
        valueMap.clear();
    }

    public boolean isEmpty() {
        return keyMap.isEmpty();
    }


    public EntryIterator<Short, Segment> getIterator() {
        return entryIterator;
    }

    private static class SegmentMapIterator implements EntryIterator<Short, Segment> {
        private final AbstractSegmentMap segmentMap;

        public SegmentMapIterator(AbstractSegmentMap segmentMap) {
            this.segmentMap = segmentMap;
        }

        @Override
        public Short getHigherKey(Short currentKey) {
            if (currentKey == null) {
                return segmentMap.firstKey();
            } else {
                return segmentMap.higherKey(currentKey);
            }
        }

        @Override
        public Short getLowerKey(Short currentKey) {
            if (currentKey == null) {
                return segmentMap.lastKey();
            } else {
                return segmentMap.lowerKey(currentKey);
            }
        }

        @Override
        public Segment getValue(Short currentKey) {
            return segmentMap.getValue(currentKey);
        }

        @Override
        public Segment removeValue(Short currentKey) {
            return segmentMap.removeAll(currentKey);
        }
    }
}
