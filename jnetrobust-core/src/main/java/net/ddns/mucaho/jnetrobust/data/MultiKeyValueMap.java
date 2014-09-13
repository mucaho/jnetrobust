package net.ddns.mucaho.jnetrobust.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;

public class MultiKeyValueMap {
	private final EntryIterator<Short, MultiKeyValue> entryIterator = 
		new MultiRefDataMapIterator();
	private final transient NavigableMap<Short, MultiKeyValue> dataMapOut;
	private final TreeMap<Short, MultiKeyValue> dataMap;
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
	
	public void putAll(MultiKeyValue data) {
		putAll(data.getDynamicReferences(), data);
	}
	protected void putAll(NavigableSet<Short> refs, MultiKeyValue data) {
		Short nextKey = refs.first();
		while (nextKey != null) {
			put(nextKey, data);
			nextKey = refs.higher(nextKey);
		}
	}
	public void put(Short ref, MultiKeyValue data) {
		if (data != null)
			data.addDynamicReference(ref);
		MultiKeyValue replacedData = dataMap.put(ref, data);

		if (replacedData != null && replacedData != data)
			replacedData.removeDynamicReference(ref);
	}


	public MultiKeyValue removeAll(Short ref) {
		return removeAll(get(ref));
	}
	protected MultiKeyValue removeAll(MultiKeyValue data) {
		if (data != null)
			removeAll(data.getDynamicReferences());
		
		return data;
	}
	protected void removeAll(NavigableSet<Short> refs) {
		Short nextKey = refs.first();
		while (nextKey != null) {
			remove(nextKey);
			nextKey = refs.higher(nextKey);
		}
	}
	public MultiKeyValue remove(Short ref) {
		MultiKeyValue data = dataMap.remove(ref);
		if (data != null)
			data.removeDynamicReference(ref);
			
		return data;
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

	public void clear() {
		clear(false);
	}
	
	public void clear(boolean thourough) {
		if (thourough) {
			Collection<MultiKeyValue> datas = dataMap.values();
			for (MultiKeyValue data: datas) {
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
	};
	
}
