package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;

import java.util.*;

public class MetadataUnitMap {
    private final NavigableMap<Short, MetadataUnit> metadataMap;
    private final NavigableMap<Short, MetadataUnit> metadataMapOut;
    private final EntryIterator<Short, MetadataUnit> entryIterator = new MultiRefDataMapIterator();


    public MetadataUnitMap(Comparator<Short> comparator) {
        this.metadataMap = new TreeMap<Short, MetadataUnit>(comparator);
        this.metadataMapOut = CollectionUtils.unmodifiableNavigableMap(metadataMap);
    }

    public MetadataUnit get(Short ref) {
        return metadataMap.get(ref);
    }

    public NavigableMap<Short, MetadataUnit> getMap() {
        return metadataMapOut;
    }

    void putAll(MetadataUnit metadata) {
        putAll(metadata.getDynamicReferences(), metadata);
    }

    void putAll(NavigableSet<Short> refs, MetadataUnit metadata) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            put(nextKey, metadata);
            nextKey = refs.higher(nextKey);
        }
    }

    void put(Short ref, MetadataUnit metadata) {
        if (metadata != null)
            metadata.addDynamicReference(ref);
        MetadataUnit replacedMetadata = metadataMap.put(ref, metadata);

        if (replacedMetadata != null && replacedMetadata != metadata)
            replacedMetadata.removeDynamicReference(ref);
    }


    MetadataUnit removeAll(Short ref) {
        return removeAll(get(ref));
    }

    MetadataUnit removeAll(MetadataUnit metadata) {
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

    MetadataUnit remove(Short ref) {
        MetadataUnit metadata = metadataMap.remove(ref);
        if (metadata != null)
            metadata.removeDynamicReference(ref);

        return metadata;
    }




    MetadataUnit putStatic(MetadataUnit metadata) {
        return metadataMap.put(metadata.getStaticReference(), metadata);
    }

    MetadataUnit removeStatic(Short ref) {
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
            Collection<MetadataUnit> metadatas = metadataMap.values();
            for (MetadataUnit metadata : metadatas) {
                metadata.clearDynamicReferences();
            }
        }
        metadataMap.clear();
    }

    public boolean isEmpty() {
        return metadataMap.isEmpty();
    }


    public EntryIterator<Short, MetadataUnit> getIterator() {
        return entryIterator;
    }

    private class MultiRefDataMapIterator implements EntryIterator<Short, MetadataUnit> {
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
        public MetadataUnit getValue(Short currentKey) {
            return get(currentKey);
        }

        @Override
        public MetadataUnit removeValue(Short currentKey) {
            return removeAll(currentKey);
        }
    }

}
