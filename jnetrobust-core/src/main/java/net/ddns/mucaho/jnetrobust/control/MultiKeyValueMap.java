package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;

import java.util.*;

public class MultiKeyValueMap {
    private final NavigableMap<Short, MultiKeyValue> dataMap;
    private final NavigableMap<Short, MultiKeyValue> dataMapOut;
    private final EntryIterator<Short, MultiKeyValue> entryIterator = new MultiRefDataMapIterator();


    public MultiKeyValueMap(Comparator<Short> comparator) {
        this.dataMap = new TreeMap<Short, MultiKeyValue>(comparator);
        this.dataMapOut = CollectionUtils.unmodifiableNavigableMap(dataMap);
    }

    public MultiKeyValue get(Short ref) {
        return dataMap.get(ref);
    }

    public NavigableMap<Short, MultiKeyValue> getMap() {
        return dataMapOut;
    }

    void putAll(MultiKeyValue data) {
        putAll(data.getDynamicReferences(), data);
    }

    void putAll(NavigableSet<Short> refs, MultiKeyValue data) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            put(nextKey, data);
            nextKey = refs.higher(nextKey);
        }
    }

    void put(Short ref, MultiKeyValue data) {
        if (data != null)
            data.addDynamicReference(ref);
        MultiKeyValue replacedData = dataMap.put(ref, data);

        if (replacedData != null && replacedData != data)
            replacedData.removeDynamicReference(ref);
    }


    MultiKeyValue removeAll(Short ref) {
        return removeAll(get(ref));
    }

    MultiKeyValue removeAll(MultiKeyValue data) {
        if (data != null)
            removeAll(data.getDynamicReferences());

        return data;
    }

    void removeAll(NavigableSet<Short> refs) {
        Short nextKey = refs.first();
        while (nextKey != null) {
            remove(nextKey);
            nextKey = refs.higher(nextKey);
        }
    }

    MultiKeyValue remove(Short ref) {
        MultiKeyValue data = dataMap.remove(ref);
        if (data != null)
            data.removeDynamicReference(ref);

        return data;
    }




    MultiKeyValue putStatic(MultiKeyValue data) {
        return dataMap.put(data.getStaticReference(), data);
    }

    MultiKeyValue removeStatic(Short ref) {
        return dataMap.remove(ref);
    }



    public int size() {
        return dataMap.size();
    }

    public Short firstKey() {
        return dataMap.isEmpty() ? null : dataMap.firstKey();
    }

    public Short higherKey(Short key) {
        return dataMap.higherKey(key);
    }

    public Short lastKey() {
        return dataMap.isEmpty() ? null : dataMap.lastKey();
    }

    public Short lowerKey(Short key) {
        return dataMap.lowerKey(key);
    }

    void clear() {
        clear(false);
    }

    void clear(boolean thourough) {
        if (thourough) {
            Collection<MultiKeyValue> datas = dataMap.values();
            for (MultiKeyValue data : datas) {
                data.clearDynamicReferences();
            }
        }
        dataMap.clear();
    }

    public boolean isEmpty() {
        return dataMap.isEmpty();
    }


    public EntryIterator<Short, MultiKeyValue> getIterator() {
        return entryIterator;
    }

    private class MultiRefDataMapIterator implements EntryIterator<Short, MultiKeyValue> {
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
        public MultiKeyValue getValue(Short currentKey) {
            return get(currentKey);
        }

        @Override
        public MultiKeyValue removeValue(Short currentKey) {
            return removeAll(currentKey);
        }
    }

}
