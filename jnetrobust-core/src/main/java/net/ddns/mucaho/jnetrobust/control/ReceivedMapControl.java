/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

public class ReceivedMapControl<T> extends MapControl<T> {
    public interface TransmissionOrderListener<T> {
        public void handleOrderedData(short dataId, T orderedData);
        public void handleUnorderedData(short dataId, T unorderedData);
    }

    private final TransmissionOrderListener<T> listener;
    private short nextDataId;

    public ReceivedMapControl(short dataId, TransmissionOrderListener<T> listener, int maxEntries, int maxEntryOffset,
                              int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
        this.nextDataId = (short) (dataId + 1);
    }


    @Override
    protected void createMap() {
        dataMap = new MetadataMap<T>(comparator) {
            @Override
            Metadata<T> putDataId(Metadata<T> metadata) {
                if (comparator.compare(metadata.getDataId(), nextDataId) >= 0) {
                    return super.putDataId(metadata);
                }
                return null;
            }
        };
    }


    public void addToReceived(Metadata<T> metadata) {
        // discard old entries in received map
        super.discardEntries();

        // add original to received map
        dataMap.putDataId(metadata);

        // remove multiple from map -> least, consecutive, ordered elements
        removeTail();
    }

    private void removeTail() {
        Short key = dataMap.firstKey();
        while (key != null && key == nextDataId) {
            notifyOrdered(dataMap.removeDataId(key));

            key = dataMap.higherKey(key);
            nextDataId++;
        }
    }

    @Override
    protected void discardEntry(short key) {
        nextDataId = comparator.compare((short) (key + 1), nextDataId) > 0 ?
                (short) (key + 1) : nextDataId;
        notifyUnordered(dataMap.removeDataId(key));
    }

    private void notifyUnordered(Metadata<T> unorderedMetadata) {
        if (unorderedMetadata != null)
            listener.handleUnorderedData(unorderedMetadata.getDataId(), unorderedMetadata.getData());
    }

    private void notifyOrdered(Metadata<T> orderedMetadata) {
        if (orderedMetadata != null)
            listener.handleOrderedData(orderedMetadata.getDataId(), orderedMetadata.getData());

    }
}
