/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;

import java.util.*;

public class MetadataMap<T> {
    private final NavigableMap<Short, Metadata<T>> metadataMap;
    private final NavigableMap<Short, Metadata<T>> metadataMapOut;
    private final EntryIterator<Short, Metadata<T>> entryIterator;


    public MetadataMap(Comparator<Short> comparator) {
        this.metadataMap = new TreeMap<Short, Metadata<T>>(comparator);
        this.metadataMapOut = CollectionUtils.unmodifiableNavigableMap(metadataMap);
        this.entryIterator = new MetadataMapIterator<T>(this);
    }

    public Metadata<T> get(Short transmissionId) {
        return metadataMap.get(transmissionId);
    }

    public NavigableMap<Short, Metadata<T>> getMap() {
        return metadataMapOut;
    }

    void putAll(Metadata<T> metadata) {
        putAll(metadata.getTransmissionIds(), metadata);
    }

    void putAll(NavigableSet<Short> transmissionIds, Metadata<T> metadata) {
        Short nextKey = transmissionIds.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = transmissionIds.higher(nextKey);
        }
    }

    void put(Short transmissionId, Metadata<T> metadata) {
        if (metadata != null)
            metadata.addTransmissionId(transmissionId);
        Metadata<T> replacedMetadata = metadataMap.put(transmissionId, metadata);

        if (replacedMetadata != null && replacedMetadata != metadata)
            replacedMetadata.removeTransmissionId(transmissionId);
    }


    Metadata<T> removeAll(Short transmissionId) {
        return removeAll(get(transmissionId));
    }

    Metadata<T> removeAll(Metadata<T> metadata) {
        if (metadata != null)
            removeAll(metadata.getTransmissionIds());

        return metadata;
    }

    void removeAll(NavigableSet<Short> transmissionIds) {
        Short nextKey = transmissionIds.first();
        while (nextKey != null) {
            remove(nextKey);
            nextKey = transmissionIds.higher(nextKey);
        }
    }

    Metadata<T> remove(Short transmissionId) {
        Metadata<T> metadata = metadataMap.remove(transmissionId);
        if (metadata != null)
            metadata.removeTransmissionId(transmissionId);

        return metadata;
    }




    Metadata<T> putDataId(Metadata<T> metadata) {
        return metadataMap.put(metadata.getDataId(), metadata);
    }

    Metadata<T> removeDataId(Short dataId) {
        return metadataMap.remove(dataId);
    }



    public int size() {
        return metadataMap.size();
    }

    public Short firstKey() {
        return metadataMap.isEmpty() ? null : metadataMap.firstKey();
    }

    public Short higherKey(Short key) {
        return metadataMap.higherKey(key);
    }

    public Short lastKey() {
        return metadataMap.isEmpty() ? null : metadataMap.lastKey();
    }

    public Short lowerKey(Short key) {
        return metadataMap.lowerKey(key);
    }

    void clear() {
        clear(false);
    }

    void clear(boolean thourough) {
        if (thourough) {
            Collection<Metadata<T>> metadatas = metadataMap.values();
            for (Metadata<T> metadata : metadatas) {
                metadata.clearTransmissionIds();
            }
        }
        metadataMap.clear();
    }

    public boolean isEmpty() {
        return metadataMap.isEmpty();
    }


    public EntryIterator<Short, Metadata<T>> getIterator() {
        return entryIterator;
    }

    private static class MetadataMapIterator<T> implements EntryIterator<Short, Metadata<T>> {
        private final MetadataMap<T> metadataMap;
        public MetadataMapIterator(MetadataMap<T> metadataMap) {
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
            return metadataMap.get(currentKey);
        }

        @Override
        public Metadata<T> removeValue(Short currentKey) {
            return metadataMap.removeAll(currentKey);
        }
    }

}
