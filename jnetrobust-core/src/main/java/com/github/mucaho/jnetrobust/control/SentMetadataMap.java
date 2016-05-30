/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.SentMetadataComparator;

import java.util.NavigableSet;

public class SentMetadataMap<T> extends AbstractMetadataMap<T> {
    public SentMetadataMap() {
        super(IdComparator.instance, SentMetadataComparator.instance);
    }

    @Override
    void putAll(Metadata<T> metadata) {
        putAll(metadata.getTransmissionIds(), metadata);
    }

    @Override
    void putAll(NavigableSet<Short> transmissionIds, Metadata<T> metadata) {
        Short nextKey = transmissionIds.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = transmissionIds.higher(nextKey);
        }
    }

    @Override
    Metadata<T> put(Short transmissionId, Metadata<T> metadata) {
        // remove from valueMap, then possibly re-add after modifying transmissionIds
        valueMap.remove(metadata);
        metadata.addTransmissionId(transmissionId);
        if (!metadata.getTransmissionIds().isEmpty())
            valueMap.put(metadata, metadata.getTransmissionIds());

        Metadata<T> replacedMetadata = keyMap.put(transmissionId, metadata);

        if (replacedMetadata != null && replacedMetadata != metadata) {
            // remove from valueMap, then possibly re-add after modifying transmissionIds
            valueMap.remove(replacedMetadata);
            replacedMetadata.removeTransmissionId(transmissionId);
            if (!replacedMetadata.getTransmissionIds().isEmpty())
                valueMap.put(replacedMetadata, replacedMetadata.getTransmissionIds());
        }

        return replacedMetadata;
    }

    @Override
    Metadata<T> put(Metadata<T> metadata) {
        if (!metadata.getTransmissionIds().isEmpty())
            return put(metadata.getLastTransmissionId(), metadata);
        else
            return null;
    }

    @Override
    Metadata<T> removeAll(Short transmissionId) {
        return removeAll(getValue(transmissionId));
    }

    @Override
    Metadata<T> removeAll(Metadata<T> metadata) {
        if (metadata != null)
            removeAll(metadata.getTransmissionIds());

        return metadata;
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
    Metadata<T> remove(Short transmissionId) {
        Metadata<T> metadata = keyMap.remove(transmissionId);
        if (metadata != null) {
            // remove from valueMap, then possibly re-add after modifying transmissionIds
            valueMap.remove(metadata);
            metadata.removeTransmissionId(transmissionId);
            if (!metadata.getTransmissionIds().isEmpty())
                valueMap.put(metadata, metadata.getTransmissionIds());
        }

        return metadata;
    }

    @Override
    void clear(boolean thourough) {
        if (thourough) {
            NavigableSet<Metadata<T>> metadatas = valueMap.navigableKeySet();
            Metadata<T> metadata = metadatas.isEmpty() ? null : metadatas.first();
            while (metadata != null) {
                metadata.clearTransmissionIds();

                metadata = metadatas.higher(metadata);
            }
        }

        super.clear(thourough);
    }
}
