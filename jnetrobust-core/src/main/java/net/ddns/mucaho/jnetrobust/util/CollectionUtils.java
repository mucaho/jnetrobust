package net.ddns.mucaho.jnetrobust.util;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class CollectionUtils {
    /**
     * Returns an unmodifiable view of the specified navigable set.
     */
    public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
        return new UnmodifiableNavigableSet<T>(s);
    }

    static class UnmodifiableIterator<E> implements Iterator<E> {
        private final Iterator<E> decoratedIterator;

        UnmodifiableIterator(Iterator<E> decoratedIterator) {
            this.decoratedIterator = decoratedIterator;
        }

        @Override
        public boolean hasNext() {
            return decoratedIterator.hasNext();
        }

        @Override
        public E next() {
            return decoratedIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    ;

    static class UnmodifiableNavigableSet<E>
            implements NavigableSet<E>, Serializable {
        private static final long serialVersionUID = 8002765856213847826L;
        private final NavigableSet<E> decoratedSet;

        UnmodifiableNavigableSet(NavigableSet<E> s) {
            decoratedSet = s;
        }

        @Override
        public E lower(E e) {
            return decoratedSet.lower(e);
        }

        @Override
        public E floor(E e) {
            return decoratedSet.floor(e);
        }

        @Override
        public E ceiling(E e) {
            return decoratedSet.ceiling(e);
        }

        @Override
        public E higher(E e) {
            return decoratedSet.higher(e);
        }

        @Override
        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new UnmodifiableNavigableSet<E>(decoratedSet.descendingSet());
        }

        @Override
        public Iterator<E> descendingIterator() {
            return new UnmodifiableIterator<E>(decoratedSet.descendingIterator());
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                      E toElement, boolean toInclusive) {
            return new UnmodifiableNavigableSet<E>(
                    decoratedSet.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<E>(decoratedSet.headSet(toElement, inclusive));
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<E>(decoratedSet.tailSet(fromElement, inclusive));
        }

        @Override
        public Comparator<? super E> comparator() {
            return decoratedSet.comparator();
        }

        @Override
        public E first() {
            return decoratedSet.first();
        }

        @Override
        public E last() {
            return decoratedSet.last();
        }

        @Override
        public int size() {
            return decoratedSet.size();
        }

        @Override
        public boolean isEmpty() {
            return decoratedSet.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return decoratedSet.contains(o);
        }

        @Override
        public Object[] toArray() {
            return decoratedSet.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return decoratedSet.toArray(a);
        }

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return decoratedSet.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<E> iterator() {
            return new UnmodifiableIterator<E>(decoratedSet.iterator());
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return Collections.unmodifiableSortedSet(decoratedSet.subSet(fromElement, toElement));
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return Collections.unmodifiableSortedSet(decoratedSet.headSet(toElement));
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return Collections.unmodifiableSortedSet(decoratedSet.tailSet(fromElement));
        }

    }

    static class UnmodifiableEntry<K, V> implements Entry<K, V> {
        private final Entry<K, V> decoratedEntry;

        UnmodifiableEntry(Entry<K, V> decoratedEntry) {
            this.decoratedEntry = decoratedEntry;
        }

        @Override
        public K getKey() {
            return decoratedEntry.getKey();
        }

        @Override
        public V getValue() {
            return decoratedEntry.getValue();
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns an unmodifiable view of the specified navigable map.
     */
    public static <K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, V> m) {
        return new UnmodifiableNavigableMap<K, V>(m);
    }

    static class UnmodifiableNavigableMap<K, V>
            implements NavigableMap<K, V>, Serializable {
        private static final long serialVersionUID = 1174661954057020481L;
        private final NavigableMap<K, V> decoratedMap;

        UnmodifiableNavigableMap(NavigableMap<K, V> m) {
            decoratedMap = m;
        }

        @Override
        public Comparator<? super K> comparator() {
            return decoratedMap.comparator();
        }

        @Override
        public K firstKey() {
            return decoratedMap.firstKey();
        }

        @Override
        public K lastKey() {
            return decoratedMap.lastKey();
        }

        @Override
        public Set<K> keySet() {
            return Collections.unmodifiableSet(decoratedMap.keySet());
        }

        @Override
        public Collection<V> values() {
            return Collections.unmodifiableCollection(decoratedMap.values());
        }

        /**
         * Do not modify the returned {@link java.util.Map.Entry entries} by using
         * {@link java.util.Map.Entry#setValue(Object) entry.setValue()}!!!
         */
        @Override
        public Set<Entry<K, V>> entrySet() {
            return Collections.unmodifiableSet(decoratedMap.entrySet());
        }

        @Override
        public int size() {
            return decoratedMap.size();
        }

        @Override
        public boolean isEmpty() {
            return decoratedMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return decoratedMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return decoratedMap.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return decoratedMap.get(key);
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Entry<K, V> lowerEntry(K key) {
            return new UnmodifiableEntry<K, V>(decoratedMap.lowerEntry(key));
        }

        @Override
        public K lowerKey(K key) {
            return decoratedMap.lowerKey(key);
        }

        @Override
        public Entry<K, V> floorEntry(K key) {
            return new UnmodifiableEntry<K, V>(decoratedMap.floorEntry(key));
        }

        @Override
        public K floorKey(K key) {
            return decoratedMap.floorKey(key);
        }

        @Override
        public Entry<K, V> ceilingEntry(K key) {
            return new UnmodifiableEntry<K, V>(decoratedMap.ceilingEntry(key));
        }

        @Override
        public K ceilingKey(K key) {
            return decoratedMap.ceilingKey(key);
        }

        @Override
        public Entry<K, V> higherEntry(K key) {
            return new UnmodifiableEntry<K, V>(decoratedMap.higherEntry(key));
        }

        @Override
        public K higherKey(K key) {
            return decoratedMap.higherKey(key);
        }

        @Override
        public Entry<K, V> firstEntry() {
            return new UnmodifiableEntry<K, V>(decoratedMap.firstEntry());
        }

        @Override
        public Entry<K, V> lastEntry() {
            return new UnmodifiableEntry<K, V>(decoratedMap.lastEntry());
        }

        @Override
        public Entry<K, V> pollFirstEntry() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Entry<K, V> pollLastEntry() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NavigableMap<K, V> descendingMap() {
            return new UnmodifiableNavigableMap<K, V>(decoratedMap.descendingMap());
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return new UnmodifiableNavigableSet<K>(decoratedMap.navigableKeySet());
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return new UnmodifiableNavigableSet<K>(decoratedMap.descendingKeySet());
        }

        @Override
        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive,
                                         K toKey, boolean toInclusive) {
            return new UnmodifiableNavigableMap<K, V>(
                    decoratedMap.subMap(fromKey, fromInclusive, toKey, toInclusive));
        }

        @Override
        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            return new UnmodifiableNavigableMap<K, V>(decoratedMap.headMap(toKey, inclusive));
        }

        @Override
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            return new UnmodifiableNavigableMap<K, V>(decoratedMap.tailMap(fromKey, inclusive));
        }

        @Override
        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            return Collections.unmodifiableSortedMap(decoratedMap.subMap(fromKey, toKey));
        }

        @Override
        public SortedMap<K, V> headMap(K toKey) {
            return Collections.unmodifiableSortedMap(decoratedMap.headMap(toKey));
        }

        @Override
        public SortedMap<K, V> tailMap(K fromKey) {
            return Collections.unmodifiableSortedMap(decoratedMap.tailMap(fromKey));
        }
    }


}
