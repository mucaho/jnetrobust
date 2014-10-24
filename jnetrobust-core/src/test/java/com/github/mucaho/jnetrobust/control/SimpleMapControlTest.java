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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleMapControlTest extends MapControlTest {
    private final HashSet<Short> discardedKeys = new HashSet<Short>();
    private final HashSet<Metadata<Object>> discardedMetadatas = new HashSet<Metadata<Object>>();

    private final ProtocolConfig<Object> config = new ProtocolConfig<Object>(null);

    private MapControl<Object> control = new MapControl<Object>(config.getPacketQueueLimit(), config.getPacketOffsetLimit(),
            config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
        @Override
        protected void discardEntry(short key) {
            discardedKeys.add(key);
            discardedMetadatas.add(dataMap.removeAll(key));
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
            } while (dataMap.get(key) != null || discardedKeys.contains(key));
            max = Math.max(key, max);

            if (metadata == null || decision.ok()) {
                metadata = new Metadata<Object>(++dataId, i);
                dataCount++;
            }
            dataMap.put(key, metadata);
        }

        return new Result(loopCount, max, dataCount);
    }

    private final void assertCommon(Result result) {
        for (Short discardedKey : discardedKeys) {
            assertTrue("discarded keys are smaller then first key of dataMap",
                    discardedKey < dataMap.firstKey());
        }
        HashSet<Metadata<Object>> allMetadatas = new HashSet<Metadata<Object>>(discardedMetadatas);
        allMetadatas.addAll(dataMap.getMap().values());
        assertEquals("total dataCount matches", result.dataCount, allMetadatas.size());
    }

    @Test
    public final void testDiscardTooManyEntries() {
        Result result = doInsertion(new Decision() {
            final Random rand = new Random();

            @Override
            public boolean ok() {
                return rand.nextBoolean();
            }
        });
        Deencapsulation.invoke(control, "discardTooManyEntries");

        assertCommon(result);
        assertTrue("dataMap key size is limited", control.maxEntries >= dataMap.size());
        assertTrue("discarded key size is in range",
                result.loopCount - control.maxEntries >= discardedKeys.size());
        assertTrue("dataMap last key is >= max key", result.max >= dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardTooManyEntries2() {
        Result result = doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return true;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyEntries");

        assertCommon(result);
        assertEquals("dataMap key size matches", control.maxEntries, dataMap.size());
        assertEquals("discarded key size matches", result.loopCount - control.maxEntries, discardedKeys.size());
        assertEquals("dataMap last key is max key", result.max, dataMap.lastKey().intValue());
    }

    @Test
    public final void testDiscardTooManyEntries3() {
        doInsertion(new Decision() {
            @Override
            public boolean ok() {
                return false;
            }
        });
        Deencapsulation.invoke(control, "discardTooManyEntries");

        assertTrue("dataMap is empty", dataMap.isEmpty());
        assertEquals("discarded key size is 1", 1, discardedKeys.size());
        assertEquals("discarded data size is 1", 1, discardedMetadatas.size());
    }

    @Test
    public final void testDiscardTooOldEntries() {
        Random rand = new Random();

        Metadata<Object> metadata = null;
        int dataCount = 0;
        for (int i = 0; i < IdComparator.MAX_SEQUENCE; i++) {
            if (metadata == null || rand.nextBoolean()) {
                metadata = new Metadata<Object>(++dataId, i);
                dataCount++;
            }

            dataMap.put((short) i, metadata);

            if (i % control.maxEntryOffset == 0) {
                Deencapsulation.invoke(control, "discardTooOldEntries");
            }
        }
        Deencapsulation.invoke(control, "discardTooOldEntries");

        assertTrue("first key of map is smaller than last key",
                IdComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) <= 0);
        assertTrue("first key of map is less than offset smaller than last key",
                IdComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) >=
                        -control.maxEntryOffset);
    }

    public final void testDiscardTimedoutEntries() {
        Deencapsulation.setField(control, "maxEntryTimeout", 2L);
    }

}