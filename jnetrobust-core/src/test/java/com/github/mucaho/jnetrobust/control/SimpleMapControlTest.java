/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.util.IdComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.*;

public class SimpleMapControlTest extends AbstractMapControlTest {
    private final HashSet<Short> discardedKeys = new HashSet<Short>();
    private final HashSet<Metadata<Object>> discardedMetadatas = new HashSet<Metadata<Object>>();

    private final ProtocolConfig config = new ProtocolConfig();

    private AbstractMapControl<Object> control = new AbstractMapControl<Object>(config.getPacketQueueLimit(), config.getPacketOffsetLimit(),
            config.getPacketRetransmitLimit(), config.getPacketQueueTimeout()) {
        @Override
        protected AbstractMetadataMap<Object> createMap() {
            return new SentMetadataMap<Object>();
        }

        @Override
        protected void discardEntry(Short key) {
            discardedKeys.addAll(dataMap.getValue(key).getTransmissionIds());
            discardedMetadatas.add(dataMap.removeAll(key));
        }

        @Override
        protected void discardEntry(Metadata<Object> metadata) {
            discardedKeys.addAll(metadata.getTransmissionIds());
            discardedMetadatas.add(dataMap.removeAll(metadata));
        }

        @Override
        protected void discardEntryKey(Short key) {
            discardedKeys.add(key);
            Metadata<Object> metadata = dataMap.remove(key);
            if (metadata != null && metadata.getTransmissionIds().isEmpty()) {
                discardedMetadatas.add(metadata);
            }
        }
    };

    public SimpleMapControlTest() {
        initDataMap(control);
    }

    @Before
    public final void prepareDiscardTest() {
        dataMap.clear();
        discardedKeys.clear();
        discardedMetadatas.clear();
    }

    private interface Decision {
        public boolean ok();
    }

    private class Result {
        public int loopCount;
        public int max;
        public int dataCount;

        public Result(int loopCount, int max, int dataCount) {
            this.loopCount = loopCount;
            this.max = max;
            this.dataCount = dataCount;
        }
    }

    private final Result doInsertion(Decision decision) {
        final Integer loopCount = config.getPacketQueueLimit() + 1;
        Random rand = new Random();

        Short key;
        Metadata<Object> metadata = null;
        int dataCount = 0, max = -IdComparator.MAX_SEQUENCE / 2;
        for (int i = 0; i < loopCount; i++) {
            do {
                key = (short) (rand.nextInt(IdComparator.MAX_SEQUENCE / 2) - IdComparator.MAX_SEQUENCE / 4);
            } while (dataMap.getValue(key) != null || discardedKeys.contains(key));
            max = Math.max(key, max);

            if (metadata == null || decision.ok()) {
                metadata = new Metadata<Object>(++dataId, i);
                dataCount++;
            }
            dataMap.put(key, metadata);
        }

        return new Result(loopCount, max, dataCount);
    }

    private final void assertCommon(int dataCount, int loopCount) {
        for (Metadata<Object> discardedMetadata : discardedMetadatas) {
            assertNull("discarded metadata is no longer in dataMap", dataMap.getKeys(discardedMetadata));
        }
        for (Short discardedKey : discardedKeys) {
            if (!dataMap.isEmpty() && loopCount < ProtocolConfig.MAX_PACKET_OFFSET_LIMIT) {
                assertTrue("discarded keys are smaller then first key of dataMap",
                        discardedKey < dataMap.firstKey());
            }
            assertNull("discarded key is no longer in dataMap", dataMap.getValue(discardedKey));
        }

        HashSet<Metadata<Object>> allMetadatas = new HashSet<Metadata<Object>>(discardedMetadatas);
        allMetadatas.addAll(dataMap.getValues());
        assertEquals("total dataCount matches", dataCount, allMetadatas.size());
    }

    @Test
    public final void testDiscardTooManyDistinctEntryValues_Random() {
        Result result = doInsertion(new Decision() {
            final Random rand = new Random();

            @Override
            public boolean ok() {
                return rand.nextBoolean();
            }
        });
        Deencapsulation.invoke(control, "discardTooManyDistinctEntryValues");

        assertCommon(result.dataCount, result.loopCount);
        assertTrue("dataMap data size is limited", control.maxEntries >= dataMap.valueSize());
        assertTrue("discarded data count is in range",
                result.loopCount - control.maxEntries >= discardedMetadatas.size());
        assertTrue("dataMap key size is gte than data size", dataMap.keySize() >= dataMap.valueSize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
        assertTrue("discarded key count is gte then discarded data count",
                discardedKeys.size() >= discardedMetadatas.size());
    }

    @Test
    public final void testDiscardTooManyDistinctEntryValues_OneKeyOneMetadata() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return true;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyDistinctEntryValues");

        assertCommon(result.dataCount, result.loopCount);
        assertEquals("dataMap data size matches max allowed data size", control.maxEntries, dataMap.valueSize());
        assertEquals("discarded data count is 1", 1, discardedMetadatas.size());
        assertEquals("dataMap key size is equal to data size", dataMap.valueSize(), dataMap.keySize());
        assertEquals("dataMap last key is max key", result.max, dataMap.lastKey().intValue());
        assertEquals("discarded key count is 1", 1, discardedKeys.size());
    }

    @Test
    public final void testDiscardTooManyDistinctEntryValues_AllKeysOneMetadata() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return false;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyDistinctEntryValues");

        assertCommon(result.dataCount, result.loopCount);
        assertEquals("dataMap data size is 1", 1, dataMap.valueSize());
        assertEquals("discarded data size is 0", 0, discardedMetadatas.size());
        assertEquals("dataMap key size matches insertion count", result.loopCount, dataMap.keySize());
        assertTrue("dataMap key size greater than max allowed data size", control.maxEntries < dataMap.keySize());
        assertEquals("dataMap last key is max key", result.max, dataMap.lastKey().intValue());
        assertEquals("discarded key size is 0", 0, discardedKeys.size());
    }

    @Test
    public final void testDiscardTooOldEntryKeys() {
        Random rand = new Random();
        Metadata<Object> metadata = null;
        int dataCount = 0;
        int loopCount;
        for (loopCount = 0; loopCount < IdComparator.MAX_SEQUENCE; loopCount++) {
            if (metadata == null || rand.nextBoolean()) {
                metadata = new Metadata<Object>(++dataId, loopCount);
                dataCount++;
            }

            dataMap.put((short) loopCount, metadata);

            if (loopCount % control.maxEntryOffset == 0) {
                Deencapsulation.invoke(control, "discardTooOldEntryKeys");
            }
        }
        Deencapsulation.invoke(control, "discardTooOldEntryKeys");

        assertCommon(dataCount, loopCount);

        assertTrue("first key of map is smaller than last key",
                IdComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) <= 0);
        assertTrue("first key of map is less than offset smaller than last key",
                IdComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) >= -control.maxEntryOffset);

        assertEquals("map key size matches max allowed key offset", control.maxEntryOffset, dataMap.keySize());
        assertTrue("map data size is lte to key size", dataMap.keySize() >= dataMap.valueSize());
        assertTrue("discarded data count is in range", dataCount >= discardedMetadatas.size());
    }

    @Test
    public final void testDiscardEntriesWithTooManyEntryKeys_Random() {
        Result result = doInsertion(new Decision() {
            final Random rand = new Random();
            @Override
            public boolean ok() {
                return rand.nextBoolean();
            }
        });
        Deencapsulation.invoke(control, "discardEntriesWithTooManyEntryKeys");

        assertCommon(result.dataCount, result.loopCount);

        assertTrue("discarded data count is in range", result.loopCount >= discardedMetadatas.size());
        assertTrue("discarded key count is gte then discarded data count",
                discardedKeys.size() >= discardedMetadatas.size());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.keySize());
        assertTrue("dataMap key size is gte than data size", dataMap.keySize() >= dataMap.valueSize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardEntriesWithTooManyEntryKeys_OneKeyOneMetadata() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return true;
            }
        });
        Deencapsulation.invoke(control, "discardEntriesWithTooManyEntryKeys");

        assertCommon(result.dataCount, result.loopCount);

        assertEquals("discarded data count is 0", 0, discardedMetadatas.size());
        assertEquals("discarded key count is 0", 0, discardedKeys.size());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.valueSize());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.keySize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardEntriesWithTooManyEntryKeys_AllKeysOneMetadata() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return false;
            }
        });
        Deencapsulation.invoke(control, "discardEntriesWithTooManyEntryKeys");

        assertCommon(result.dataCount, result.loopCount);

        assertEquals("discarded data count is 1", 1, discardedMetadatas.size());
        assertEquals("discarded key count is loop count", result.loopCount, discardedKeys.size());
        assertTrue("datamap is empty", dataMap.isEmpty());
        assertEquals("dataMap key size is 0", 0, dataMap.keySize());
        assertEquals("dataMap value size is 0", 0, dataMap.valueSize());
    }

    @Test
    public final void testDiscardTimedoutEntries() throws InterruptedException {
        Deencapsulation.setField(control, "maxEntryTimeout", 1000L);

        Metadata<Object> metadata1 = new Metadata<Object>(++dataId, 1);
        dataMap.put((short) 1, metadata1);

        Thread.sleep(2000L);

        Metadata<Object> metadata2 = new Metadata<Object>(++dataId, 2);
        dataMap.put((short) 2, metadata2);

        Deencapsulation.invoke(control, "discardTimedoutEntries");

        assertTrue("timestamp of 2nd metadata is newer than 1s", System.currentTimeMillis() - metadata2.getTime() < 1000L);
        assertEquals("dataMap key size is 1", 1, dataMap.keySize());
        assertEquals("dataMap key is 2nd metadata's key", (short) 2, (short) dataMap.firstKey());
        assertEquals("dataMap value size is 1", 1, dataMap.valueSize());
        assertEquals("dataMap value is 2nd metadata", metadata2, dataMap.firstValue());

        assertTrue("timestamp of 1st metadata is older than 1s", System.currentTimeMillis() - metadata1.getTime() > 1000L);
        assertEquals("discarded key size is 1", 1, discardedKeys.size());
        assertEquals("discarded key is 1st metadata's key", (short) 1, (short) discardedKeys.iterator().next());
        assertEquals("discarded data size is 1", 1, discardedMetadatas.size());
        assertEquals("dataMap value is 1st metadata", metadata1, discardedMetadatas.iterator().next());
    }
}
