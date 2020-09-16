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

public abstract class AbstractSegmentMap<T> {
    protected final NavigableMap<Short, Segment<T>> keyMap;
    private final NavigableMap<Short, Segment<T>> keyMapOut;

    protected final NavigableMap<Segment<T>, NavigableSet<Short>> valueMap;
    private final NavigableMap<Segment<T>, NavigableSet<Short>> valueMapOut;

    private final EntryIterator<Short, Segment<T>> entryIterator;

    public AbstractSegmentMap(Comparator<Short> idComparator, Comparator<Segment<?>> segmentComparator) {
        this.keyMap = new TreeMap<Short, Segment<T>>(idComparator);
        this.keyMapOut = CollectionUtils.unmodifiableNavigableMap(keyMap);

        this.valueMap = new TreeMap<Segment<T>, NavigableSet<Short>>(segmentComparator);
        this.valueMapOut = CollectionUtils.unmodifiableNavigableMap(valueMap);

        this.entryIterator = new SegmentMapIterator<T>(this);
    }

    public NavigableMap<Short, Segment<T>> getKeyMap() {
        return keyMapOut;
    }

    public NavigableMap<Segment<T>, NavigableSet<Short>> getValueMap() {
        return valueMapOut;
    }

    public NavigableSet<Short> getKeys() {
        return keyMapOut.navigableKeySet();
    }

    public NavigableSet<Segment<T>> getValues() {
        return valueMapOut.navigableKeySet();
    }

    public Segment<T> getValue(Short key) {
        return keyMapOut.get(key);
    }

    public NavigableSet<Short> getKeys(Segment<T> segment) {
        return valueMapOut.get(segment);
    }

    abstract void putAll(Segment<T> segment);

    abstract void putAll(NavigableSet<Short> keys, Segment<T> segment);

    abstract Segment<T> put(Short key, Segment<T> segment);

    abstract Segment<T> put(Segment<T> segment);

    abstract Segment<T> removeAll(Short key);

    abstract Segment<T> removeAll(Segment<T> segment);

    abstract void removeAll(NavigableSet<Short> keys);

    abstract Segment<T> remove(Short key);

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

    public Segment<T> firstValue() {
        return valueMap.isEmpty() ? null : valueMap.firstKey();
    }

    public Segment<T> higherValue(Segment<T> segment) {
        return valueMap.higherKey(segment);
    }

    public Segment<T> lastValue() {
        return valueMap.isEmpty() ? null : valueMap.lastKey();
    }

    public Segment<T> lowerValue(Segment<T> segment) {
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


    public EntryIterator<Short, Segment<T>> getIterator() {
        return entryIterator;
    }

    private static class SegmentMapIterator<T> implements EntryIterator<Short, Segment<T>> {
        private final AbstractSegmentMap<T> segmentMap;

        public SegmentMapIterator(AbstractSegmentMap<T> segmentMap) {
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
        public Segment<T> getValue(Short currentKey) {
            return segmentMap.getValue(currentKey);
        }

        @Override
        public Segment<T> removeValue(Short currentKey) {
            return segmentMap.removeAll(currentKey);
        }
    }
}
