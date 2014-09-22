package net.ddns.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;
import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.util.BitConstants;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleMapControlTest extends MapControlTest {
    private final HashSet<Short> discardedKeys = new HashSet<Short>();
    private final HashSet<MultiKeyValue> discardedDatas = new HashSet<MultiKeyValue>();

    private final ProtocolConfig config = new ProtocolConfig(null);

    private MapControl control = new MapControl(config.getPacketQueueLimit(), config.getPacketOffsetLimit(),
            config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout()) {
        @Override
        protected void discardEntry(short key) {
            discardedKeys.add(key);
            discardedDatas.add(dataMap.removeAll(key));
        }
    };

    public SimpleMapControlTest() {
        initDataMap(control);
    }

    @Before
    public final void prepareDiscardTest() {
        dataMap.clear();
        discardedKeys.clear();
        discardedDatas.clear();
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
        final Integer loopCount = config.getPacketQueueLimit();
        Random rand = new Random();

        Short key;
        MultiKeyValue data = null;
        int dataCount = 0, max = -SequenceComparator.MAX_SEQUENCE / 2;
        for (int i = 0; i < loopCount; i++) {
            do {
                key = (short) (rand.nextInt(SequenceComparator.MAX_SEQUENCE / 2) - SequenceComparator.MAX_SEQUENCE / 4);
            } while (dataMap.get(key) != null || discardedKeys.contains(key));
            max = Math.max(key, max);

            if (data == null || decision.ok()) {
                data = new MultiKeyValue(++dataId, i);
                dataCount++;
            }
            dataMap.put(key, data);
        }

        return new Result(loopCount, max, dataCount);
    }

    private final void assertCommon(Result result) {
        for (Short discardedKey : discardedKeys) {
            assertTrue("discarded keys are smaller then first key of dataMap",
                    discardedKey < dataMap.firstKey());
        }
        HashSet<MultiKeyValue> allDatas = new HashSet<MultiKeyValue>(discardedDatas);
        allDatas.addAll(dataMap.getMap().values());
        assertEquals("total dataCount matches", result.dataCount, allDatas.size());
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
        assertEquals("discarded data size is 1", 1, discardedDatas.size());
    }

    @Test
    public final void testDiscardTooOldEntries() {
        Random rand = new Random();

        MultiKeyValue data = null;
        int dataCount = 0;
        for (int i = 0; i < SequenceComparator.MAX_SEQUENCE; i++) {
            if (data == null || rand.nextBoolean()) {
                data = new MultiKeyValue(++dataId, i);
                dataCount++;
            }

            dataMap.put((short) i, data);

            if (i % control.maxEntryOffset == 0) {
                Deencapsulation.invoke(control, "discardTooOldEntries");
            }
        }
        Deencapsulation.invoke(control, "discardTooOldEntries");

        assertTrue("first key of map is smaller than last key",
                SequenceComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) <= 0);
        assertTrue("first key of map is less than offset smaller than last key",
                SequenceComparator.instance.compare(dataMap.firstKey(), dataMap.lastKey()) >=
                        -control.maxEntryOffset);
    }

    public final void testDiscardTimedoutEntries() {
        Deencapsulation.setField(control, "maxEntryTimeout", 2L);
    }

}
