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
        this.entryIterator = new MultiRefDataMapIterator<T>(this);
    }

    public Metadata<T> get(Short ref) {
        return metadataMap.get(ref);
    }

    public NavigableMap<Short, Metadata<T>> getMap() {
        return metadataMapOut;
    }

    void putAll(Metadata<T> metadata) {
        putAll(metadata.getDynamicReferences(), metadata);
    }

    void putAll(NavigableSet<Short> refs, Metadata<T> metadata) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = refs.higher(nextKey);
        }
    }

    void put(Short ref, Metadata<T> metadata) {
        if (metadata != null)
            metadata.addDynamicReference(ref);
        Metadata<T> replacedMetadata = metadataMap.put(ref, metadata);

        if (replacedMetadata != null && replacedMetadata != metadata)
            replacedMetadata.removeDynamicReference(ref);
    }


    Metadata<T> removeAll(Short ref) {
        return removeAll(get(ref));
    }

    Metadata<T> removeAll(Metadata<T> metadata) {
        if (metadata != null)
            removeAll(metadata.getDynamicReferences());

        return metadata;
    }

    void removeAll(NavigableSet<Short> refs) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            remove(nextKey);
            nextKey = refs.higher(nextKey);
        }
    }

    Metadata<T> remove(Short ref) {
        Metadata<T> metadata = metadataMap.remove(ref);
        if (metadata != null)
            metadata.removeDynamicReference(ref);

        return metadata;
    }




    Metadata<T> putStatic(Metadata<T> metadata) {
        return metadataMap.put(metadata.getStaticReference(), metadata);
    }

    Metadata<T> removeStatic(Short ref) {
        return metadataMap.remove(ref);
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
                metadata.clearDynamicReferences();
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

    private static class MultiRefDataMapIterator<T> implements EntryIterator<Short, Metadata<T>> {
        private final MetadataMap<T> metadataMap;
        public MultiRefDataMapIterator(MetadataMap<T> metadataMap) {
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
