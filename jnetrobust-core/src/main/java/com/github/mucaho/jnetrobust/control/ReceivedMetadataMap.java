/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.ReceivedMetadataComparator;

import java.util.NavigableSet;

public class ReceivedMetadataMap<T> extends AbstractMetadataMap<T> {

    public ReceivedMetadataMap() {
        super(IdComparator.instance, ReceivedMetadataComparator.instance);
    }

    @Override
    void putAll(Metadata<T> metadata) {
        putAll(metadata.getDataIds(), metadata);
    }

    @Override
    void putAll(NavigableSet<Short> dataIds, Metadata<T> metadata) {
        Short nextKey = dataIds.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = dataIds.higher(nextKey);
        }
    }

    @Override
    Metadata<T> put(Short dataId, Metadata<T> metadata) {
        valueMap.put(metadata, metadata.getDataIds());
        return keyMap.put(dataId, metadata);
    }

    @Override
    Metadata<T> put(Metadata<T> metadata) {
        return put(metadata.getDataId(), metadata);
    }

    @Override
    Metadata<T> removeAll(Short dataId) {
        return removeAll(getValue(dataId));
    }

    @Override
    Metadata<T> removeAll(Metadata<T> metadata) {
        if (metadata != null)
            removeAll(metadata.getDataIds());

        return metadata;
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
    Metadata<T> remove(Short dataId) {
        Metadata<T> removedMetadata = keyMap.remove(dataId);
        if (removedMetadata != null)
            valueMap.remove(removedMetadata);
        return removedMetadata;
    }
}
