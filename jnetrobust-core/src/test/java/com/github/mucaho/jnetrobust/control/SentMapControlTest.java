/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.SystemClock;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static com.github.mucaho.jnetrobust.util.TestUtils.binInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class SentMapControlTest extends AbstractMapControlTest {

    protected SentMapControl handler = new SentMapControl(null, config.getPacketQueueLimit(),
            config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1,
            config.getPacketQueueTimeout(), new SystemClock() {
        @Override
        public long getTimeNow() {
            return System.currentTimeMillis();
        }
    });

    public SentMapControlTest() {
        initDataMap(handler);
    }

    @Before
    public final void prepareTest() {
        dataMap.clear();
    }

    public Object[][] parametersForTestRemoveFromPending() {
        // MultiRefObject:
        // * it is important what the first reference is (first short in each line)
        // * order of insertion must be equal to order of removal for testing purposes (order of lines)

        Object[][] out = (Object[][])
                $($(
                        (short) 10, binInt("0"), $($S(10))
                ), $(
                        (short) 10, binInt("1"), $($S(9))
                ), $(
                        (short) 10, binInt("1111111111111"), $($S(10))
                ), $(
                        (short) 10, binInt("1111111111111"), $($S(-1))
                ), $(
                        (short) 10, binInt("1"), $($S(9, 10))
                ), $(
                        (short) 10, binInt("101"), $($S(7, 8, 9, 10))
                ), $(
                        (short) 10, binInt("101"), $($S(7), $S(9))
                ), $(
                        (short) 10, binInt("100"), $($S(7), $S(10))
                ), $(
                        (short) 10, binInt("101"), $($S(7), $S(9), $S(10))
                ), $(
                        (short) 10, binInt("101"), $($S(7, 21), $S(9, 19), $S(10, 20))
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testRemoveFromPending(short localSeq, int lastLocalSeqs,
                                            final Short[][] referenceGroups) {


        class Wrapper {
            public int invocations = 0;
        }
        final Wrapper wrapper = new Wrapper();
        new MockUp<SentMapControl>() {
            @Mock
            @SuppressWarnings("unused")
            protected void notifyAcked(Invocation invocation, Short transmissionId, Segment ackedSegment, boolean directlyAcked) {
                if (ackedSegment != null) {
                    assertEquals("Expected other value (insertion order must be remove order)",
                            wrapper.invocations, deserializeShort(ackedSegment.getData()));
                    wrapper.invocations++;
                }
            }
        };

        for (int i = 0; i < referenceGroups.length; ++i) {
            addData(i, referenceGroups[i]);
        }
        handler.removeFromSent(localSeq, lastLocalSeqs);

        assertEquals("Invocation count should match data count.", wrapper.invocations, referenceGroups.length);
        assertTrue("DataMap should be empty.", dataMap.isEmpty());
    }

    public Object[][] parametersForTestAddToPending() {
        Object[][] out = (Object[][])
                $($(
                        $($S(10))
                ), $(
                        $($S(7, 8, 9, 10))
                ), $(
                        $($S(7, 21), $S(9, 19), $S(10, 20))
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testAddToPending(final Short[][] referenceGroups) {
        for (Short[] referenceGroup : referenceGroups) {
            Segment segment = new Segment(++dataId, serializeShorts(referenceGroup));
            for (Short reference : referenceGroup) {
                handler.addToSent(reference, segment);
            }
        }

        int dataCount = 0;
        int referenceCount = 0;
        for (Short[] referenceGroup : referenceGroups) {
            dataCount++;
            referenceCount += referenceGroup.length;
        }
        assertEquals("Total data count match", dataCount,
                new HashSet<Segment>(handler.dataMap.getKeyMap().values()).size());
        assertEquals("Total reference count match", referenceCount, handler.dataMap.getKeyMap().keySet().size());


        for (Segment segment : handler.dataMap.getKeyMap().values()) {
            Short[] dataValues = deserializeShorts(segment.getData());
            assertEquals("Reference count match", dataValues.length, segment.getTransmissionIds().size());
            for (Short dataValue : dataValues) {
                assertTrue("Reference match", segment.getTransmissionIds().contains(dataValue));
            }
        }

    }

}
