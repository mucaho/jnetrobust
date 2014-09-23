package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;

import java.util.*;

public class MetadataMap {
    private final NavigableMap<Short, Metadata> metadataMap;
    private final NavigableMap<Short, Metadata> metadataMapOut;
    private final EntryIterator<Short, Metadata> entryIterator = new MultiRefDataMapIterator();


    public MetadataMap(Comparator<Short> comparator) {
        this.metadataMap = new TreeMap<Short, Metadata>(comparator);
        this.metadataMapOut = CollectionUtils.unmodifiableNavigableMap(metadataMap);
    }

    public Metadata get(Short ref) {
        return metadataMap.get(ref);
    }

    public NavigableMap<Short, Metadata> getMap() {
        return metadataMapOut;
    }

    void putAll(Metadata metadata) {
        putAll(metadata.getDynamicReferences(), metadata);
    }

    void putAll(NavigableSet<Short> refs, Metadata metadata) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = refs.higher(nextKey);
        }
    }

    void put(Short ref, Metadata metadata) {
        if (metadata != null)
            metadata.addDynamicReference(ref);
        Metadata replacedMetadata = metadataMap.put(ref, metadata);

        if (replacedMetadata != null && replacedMetadata != metadata)
            replacedMetadata.removeDynamicReference(ref);
    }


    Metadata removeAll(Short ref) {
        return removeAll(get(ref));
    }

    Metadata removeAll(Metadata metadata) {
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

    Metadata remove(Short ref) {
        Metadata metadata = metadataMap.remove(ref);
        if (metadata != null)
            metadata.removeDynamicReference(ref);

        return metadata;
    }




    Metadata putStatic(Metadata metadata) {
        return metadataMap.put(metadata.getStaticReference(), metadata);
    }

    Metadata removeStatic(Short ref) {
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
            Collection<Metadata> metadatas = metadataMap.values();
            for (Metadata metadata : metadatas) {
                metadata.clearDynamicReferences();
            }
        }
        metadataMap.clear();
    }

    public boolean isEmpty() {
        return metadataMap.isEmpty();
    }


    public EntryIterator<Short, Metadata> getIterator() {
        return entryIterator;
    }

    private class MultiRefDataMapIterator implements EntryIterator<Short, Metadata> {
        @Override
        public Short getHigherKey(Short currentKey) {
            if (currentKey == null) {
                return firstKey();
            } else {
                return higherKey(currentKey);
            }
        }

        @Override
        public Short getLowerKey(Short currentKey) {
            if (currentKey == null) {
                return lastKey();
            } else {
                return lowerKey(currentKey);
            }
        }

        @Override
        public Metadata getValue(Short currentKey) {
            return get(currentKey);
        }

        @Override
        public Metadata removeValue(Short currentKey) {
            return removeAll(currentKey);
        }
    }

}
