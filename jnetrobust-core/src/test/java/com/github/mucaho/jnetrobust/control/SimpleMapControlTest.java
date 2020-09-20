/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.SystemClock;
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
    private final HashSet<Segment> discardedSegments = new HashSet<Segment>();

    private final ProtocolConfig config = new ProtocolConfig();

    private AbstractMapControl control = new AbstractMapControl(config.getPacketQueueLimit(), config.getPacketOffsetLimit(),
            config.getPacketRetransmitLimit(), config.getPacketQueueTimeout(), new SystemClock() {
        @Override
        public long getTimeNow() {
            return System.currentTimeMillis();
        }
    }) {
        @Override
        protected AbstractSegmentMap createMap() {
            return new SentSegmentMap();
        }

        @Override
        protected void discardEntry(Short key) {
            discardedKeys.addAll(dataMap.getValue(key).getTransmissionIds());
            discardedSegments.add(dataMap.removeAll(key));
        }

        @Override
        protected void discardEntry(Segment segment) {
            discardedKeys.addAll(segment.getTransmissionIds());
            discardedSegments.add(dataMap.removeAll(segment));
        }

        @Override
        protected void discardEntryKey(Short key) {
            discardedKeys.add(key);
            Segment segment = dataMap.remove(key);
            if (segment != null && segment.getTransmissionIds().isEmpty()) {
                discardedSegments.add(segment);
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
        discardedSegments.clear();
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
        Segment segment = null;
        int dataCount = 0, max = -IdComparator.MAX_SEQUENCE / 2;
        for (int i = 0; i < loopCount; i++) {
            do {
                key = (short) (rand.nextInt(IdComparator.MAX_SEQUENCE / 2) - IdComparator.MAX_SEQUENCE / 4);
            } while (dataMap.getValue(key) != null || discardedKeys.contains(key));
            max = Math.max(key, max);

            if (segment == null || decision.ok()) {
                segment = new Segment(++dataId, serializeInt(i));
                dataCount++;
            }
            dataMap.put(key, segment);
        }

        return new Result(loopCount, max, dataCount);
    }

    private final void assertCommon(int dataCount, int loopCount) {
        for (Segment discardedSegment : discardedSegments) {
            assertNull("discarded segment is no longer in dataMap", dataMap.getKeys(discardedSegment));
        }
        for (Short discardedKey : discardedKeys) {
            if (!dataMap.isEmpty() && loopCount < ProtocolConfig.MAX_PACKET_OFFSET_LIMIT) {
                assertTrue("discarded keys are smaller then first key of dataMap",
                        discardedKey < dataMap.firstKey());
            }
            assertNull("discarded key is no longer in dataMap", dataMap.getValue(discardedKey));
        }

        HashSet<Segment> allSegments = new HashSet<Segment>(discardedSegments);
        allSegments.addAll(dataMap.getValues());
        assertEquals("total dataCount matches", dataCount, allSegments.size());
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
                result.loopCount - control.maxEntries >= discardedSegments.size());
        assertTrue("dataMap key size is gte than data size", dataMap.keySize() >= dataMap.valueSize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
        assertTrue("discarded key count is gte then discarded data count",
                discardedKeys.size() >= discardedSegments.size());
    }

    @Test
    public final void testDiscardTooManyDistinctEntryValues_OneKeyOneSegment() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return true;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyDistinctEntryValues");

        assertCommon(result.dataCount, result.loopCount);
        assertEquals("dataMap data size matches max allowed data size", control.maxEntries, dataMap.valueSize());
        assertEquals("discarded data count is 1", 1, discardedSegments.size());
        assertEquals("dataMap key size is equal to data size", dataMap.valueSize(), dataMap.keySize());
        assertEquals("dataMap last key is max key", result.max, dataMap.lastKey().intValue());
        assertEquals("discarded key count is 1", 1, discardedKeys.size());
    }

    @Test
    public final void testDiscardTooManyDistinctEntryValues_AllKeysOneSegment() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return false;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyDistinctEntryValues");

        assertCommon(result.dataCount, result.loopCount);
        assertEquals("dataMap data size is 1", 1, dataMap.valueSize());
        assertEquals("discarded data size is 0", 0, discardedSegments.size());
        assertEquals("dataMap key size matches insertion count", result.loopCount, dataMap.keySize());
        assertTrue("dataMap key size greater than max allowed data size", control.maxEntries < dataMap.keySize());
        assertEquals("dataMap last key is max key", result.max, dataMap.lastKey().intValue());
        assertEquals("discarded key size is 0", 0, discardedKeys.size());
    }

    @Test
    public final void testDiscardTooOldEntryKeys() {
        Random rand = new Random();
        Segment segment = null;
        int dataCount = 0;
        int loopCount;
        for (loopCount = 0; loopCount < IdComparator.MAX_SEQUENCE; loopCount++) {
            if (segment == null || rand.nextBoolean()) {
                segment = new Segment(++dataId, serializeInt(loopCount));
                dataCount++;
            }

            dataMap.put((short) loopCount, segment);

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
        assertTrue("discarded data count is in range", dataCount >= discardedSegments.size());
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

        assertTrue("discarded data count is in range", result.loopCount >= discardedSegments.size());
        assertTrue("discarded key count is gte then discarded data count",
                discardedKeys.size() >= discardedSegments.size());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.keySize());
        assertTrue("dataMap key size is gte than data size", dataMap.keySize() >= dataMap.valueSize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardEntriesWithTooManyEntryKeys_OneKeyOneSegment() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return true;
            }
        });
        Deencapsulation.invoke(control, "discardEntriesWithTooManyEntryKeys");

        assertCommon(result.dataCount, result.loopCount);

        assertEquals("discarded data count is 0", 0, discardedSegments.size());
        assertEquals("discarded key count is 0", 0, discardedKeys.size());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.valueSize());
        assertEquals("dataMap key size is same as loopCount", result.loopCount, dataMap.keySize());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardEntriesWithTooManyEntryKeys_AllKeysOneSegment() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return false;
            }
        });
        Deencapsulation.invoke(control, "discardEntriesWithTooManyEntryKeys");

        assertCommon(result.dataCount, result.loopCount);

        assertEquals("discarded data count is 1", 1, discardedSegments.size());
        assertEquals("discarded key count is loop count", result.loopCount, discardedKeys.size());
        assertTrue("datamap is empty", dataMap.isEmpty());
        assertEquals("dataMap key size is 0", 0, dataMap.keySize());
        assertEquals("dataMap value size is 0", 0, dataMap.valueSize());
    }

    @Test
    public final void testDiscardTimedoutEntries() throws InterruptedException {
        Deencapsulation.setField(control, "maxEntryTimeout", 1000L);

        Segment segment1 = new Segment(++dataId, serializeInt(1));
        segment1.setNewestSentTime(System.currentTimeMillis());
        dataMap.put((short) 1, segment1);

        Thread.sleep(2000L);

        Segment segment2 = new Segment(++dataId, serializeInt(2));
        segment2.setNewestSentTime(System.currentTimeMillis());
        dataMap.put((short) 2, segment2);

        Deencapsulation.invoke(control, "discardTimedoutEntries");

        assertTrue("timestamp of 2nd segment is newer than 1s", System.currentTimeMillis() - segment2.getNewestSentTime() < 1000L);
        assertEquals("dataMap key size is 1", 1, dataMap.keySize());
        assertEquals("dataMap key is 2nd segment's key", (short) 2, (short) dataMap.firstKey());
        assertEquals("dataMap value size is 1", 1, dataMap.valueSize());
        assertEquals("dataMap value is 2nd segment", segment2, dataMap.firstValue());

        assertTrue("timestamp of 1st segment is older than 1s", System.currentTimeMillis() - segment1.getNewestSentTime() > 1000L);
        assertEquals("discarded key size is 1", 1, discardedKeys.size());
        assertEquals("discarded key is 1st segment's key", (short) 1, (short) discardedKeys.iterator().next());
        assertEquals("discarded data size is 1", 1, discardedSegments.size());
        assertEquals("dataMap value is 1st segment", segment1, discardedSegments.iterator().next());
    }
}
