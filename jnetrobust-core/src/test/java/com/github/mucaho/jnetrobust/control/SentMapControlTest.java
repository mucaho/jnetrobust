/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

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

    protected SentMapControl<Object> handler = new SentMapControl<Object>(null, config.getPacketQueueLimit(),
            config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

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
        new MockUp<SentMapControl<Object>>() {
            @Mock
            @SuppressWarnings("unused")
            protected void notifyAcked(Invocation invocation, Segment<Object> ackedSegment, boolean directlyAcked) {
                if (ackedSegment != null) {
                    assertEquals("Expected other value (insertion order must be remove order)",
                            wrapper.invocations, ackedSegment.getData());
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
            Segment<Object> segment = new Segment<Object>(++dataId, referenceGroup);
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
                new HashSet<Segment<Object>>(handler.dataMap.getKeyMap().values()).size());
        assertEquals("Total reference count match", referenceCount, handler.dataMap.getKeyMap().keySet().size());


        for (Segment<Object> segment : handler.dataMap.getKeyMap().values()) {
            Short[] dataValues = (Short[]) segment.getData();
            assertEquals("Reference count match", dataValues.length, segment.getTransmissionIds().size());
            for (Short dataValue : dataValues) {
                assertTrue("Reference match", segment.getTransmissionIds().contains(dataValue));
            }
        }

    }

}
