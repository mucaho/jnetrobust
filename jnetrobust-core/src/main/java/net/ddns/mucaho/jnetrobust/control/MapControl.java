package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValueMap;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;
import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;


public abstract class MapControl {
    protected final static Comparator<Short> comparator = SequenceComparator.instance;

    private final TimeoutHandler<MultiKeyValue> entryTimeoutHandler =
            new TimeoutHandler<MultiKeyValue>();

    protected final int maxEntries;
    protected final int maxEntryOffset;
    protected final int maxEntryOccurrences;
    protected final long maxEntryTimeout;

    /*
     * [lastSeq]-...-[seq-32]-[seq-31]-...-[seq-1]-[seq]
     */
    protected MultiKeyValueMap dataMap;

    public MapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout) {
        this.maxEntries = maxEntries;
        this.maxEntryOffset = maxEntryOffset;
        this.maxEntryOccurrences = maxEntryOccurrences;
        this.maxEntryTimeout = maxEntryTimeout;
        createMap();
    }

    protected void createMap() {
        dataMap = new MultiKeyValueMap(comparator);
    }

    public NavigableMap<Short, MultiKeyValue> getMap() {
        return dataMap.getMap();
    }

    protected abstract void discardEntry(short key);

    protected void discardEntries() {
        discardTooManyEntries();
        discardTooOldEntries();
        discardTimedoutEntries();
        discardEntryTooOften();
    }


    private void discardTooManyEntries() {
        while (dataMap.size() > maxEntries) {
            discardEntry(dataMap.firstKey());
        }
    }

    private void discardTooOldEntries() {
        if (!dataMap.isEmpty()) {
            short newestKey = dataMap.lastKey();
            short oldestKey = dataMap.firstKey();
            while (comparator.compare(newestKey, oldestKey) >= maxEntryOffset) {
                discardEntry(oldestKey);
                oldestKey = dataMap.firstKey();
            }
        }
    }

    private void discardTimedoutEntries() {
        if (maxEntryTimeout > 0) {
            Collection<MultiKeyValue> timedOuts =
                    entryTimeoutHandler.filterTimedOut(dataMap.getMap().values(), maxEntryTimeout);
            for (MultiKeyValue timedOut : timedOuts)
                discardEntry(timedOut.getFirstDynamicReference());
        }
    }

    private void discardEntryTooOften() {
        if (maxEntryOccurrences > 0) {
            Short key = dataMap.firstKey();
            while (key != null) {
                if (dataMap.get(key).getDynamicReferences().size() > maxEntryOccurrences)
                    discardEntry(key);
                key = dataMap.higherKey(key);
            }
        }
    }
}
