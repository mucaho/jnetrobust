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

public abstract class AbstractMetadataMap<T> {
    protected final NavigableMap<Short, Metadata<T>> keyMap;
    private final NavigableMap<Short, Metadata<T>> keyMapOut;

    protected final NavigableMap<Metadata<T>, NavigableSet<Short>> valueMap;
    private final NavigableMap<Metadata<T>, NavigableSet<Short>> valueMapOut;

    private final EntryIterator<Short, Metadata<T>> entryIterator;

    public AbstractMetadataMap(Comparator<Short> idComparator, Comparator<Metadata<?>> metadataComparator) {
        this.keyMap = new TreeMap<Short, Metadata<T>>(idComparator);
        this.keyMapOut = CollectionUtils.unmodifiableNavigableMap(keyMap);

        this.valueMap = new TreeMap<Metadata<T>, NavigableSet<Short>>(metadataComparator);
        this.valueMapOut = CollectionUtils.unmodifiableNavigableMap(valueMap);

        this.entryIterator = new MetadataMapIterator<T>(this);
    }

    public NavigableMap<Short, Metadata<T>> getKeyMap() {
        return keyMapOut;
    }

    public NavigableMap<Metadata<T>, NavigableSet<Short>> getValueMap() {
        return valueMapOut;
    }

    public NavigableSet<Short> getKeys() {
        return keyMapOut.navigableKeySet();
    }

    public NavigableSet<Metadata<T>> getValues() {
        return valueMapOut.navigableKeySet();
    }

    public Metadata<T> getValue(Short key) {
        return keyMapOut.get(key);
    }

    public NavigableSet<Short> getKeys(Metadata<T> metadata) {
        return valueMapOut.get(metadata);
    }

    abstract void putAll(Metadata<T> metadata);

    abstract void putAll(NavigableSet<Short> keys, Metadata<T> metadata);

    abstract Metadata<T> put(Short key, Metadata<T> metadata);

    abstract Metadata<T> put(Metadata<T> metadata);

    abstract Metadata<T> removeAll(Short key);

    abstract Metadata<T> removeAll(Metadata<T> metadata);

    abstract void removeAll(NavigableSet<Short> keys);

    abstract Metadata<T> remove(Short key);

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

    public Metadata<T> firstValue() {
        return valueMap.isEmpty() ? null : valueMap.firstKey();
    }

    public Metadata<T> higherValue(Metadata<T> metadata) {
        return valueMap.higherKey(metadata);
    }

    public Metadata<T> lastValue() {
        return valueMap.isEmpty() ? null : valueMap.lastKey();
    }

    public Metadata<T> lowerValue(Metadata<T> metadata) {
        return valueMap.lowerKey(metadata);
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


    public EntryIterator<Short, Metadata<T>> getIterator() {
        return entryIterator;
    }

    private static class MetadataMapIterator<T> implements EntryIterator<Short, Metadata<T>> {
        private final AbstractMetadataMap<T> metadataMap;

        public MetadataMapIterator(AbstractMetadataMap<T> metadataMap) {
            this.metadataMap = metadataMap;
        }

        @Override
        public Short getHigherKey(Short currentKey) {
            if (currentKey == null) {
                return metadataMap.firstKey();
            } else {
                return metadataMap.higherKey(currentKey);
            }
        }

        @Override
        public Short getLowerKey(Short currentKey) {
            if (currentKey == null) {
                return metadataMap.lastKey();
            } else {
                return metadataMap.lowerKey(currentKey);
            }
        }

        @Override
        public Metadata<T> getValue(Short currentKey) {
            return metadataMap.getValue(currentKey);
        }

        @Override
        public Metadata<T> removeValue(Short currentKey) {
            return metadataMap.removeAll(currentKey);
        }
    }
}
