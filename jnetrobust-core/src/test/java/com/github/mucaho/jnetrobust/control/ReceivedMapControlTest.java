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
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashSet;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class ReceivedMapControlTest extends AbstractMapControlTest {
    protected static ReceivedMapControl handler = new ReceivedMapControl((short) 0, null,
            config.getPacketQueueLimit(), config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1,
            config.getPacketQueueTimeout(), new SystemClock() {
        @Override
        public long getTimeNow() {
            return System.currentTimeMillis();
        }
    });

    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    @BeforeClass
    public static void initMap() {
        initDataMap(handler);
        dataId = 0;
        Deencapsulation.setField(handler, "nextDataId", (short) 1);
        dataMap.clear();
    }


    public Object[][] parametersForTestRemoveTail() {
        Object[][] out = (Object[][])
                $($(
                        (short) 3, $S, (short) 1
                ), $(
                        (short) 2, $S, (short) 1
                ), $(
                        (short) 1, $S(1, 2, 3), (short) 4
                ), $(
                        (short) 4, $S(4), (short) 5
                ), $(
                        (short) 6, $S, (short) 5
                ), $(
                        (short) 7, $S, (short) 5
                ), $(
                        (short) 5, $S(5, 6, 7), (short) 8
                ), $(
                        (short) 1, $S, (short) 8
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testRemoveTail(final Short input, final Short[] outputs, final Short nextRemoteSeq) {

        final LinkedHashSet<Segment> orderedSegments = new LinkedHashSet<Segment>();
        new MockUp<ReceivedMapControl>() {
            @SuppressWarnings("unused")
            @Mock
            private void notifyOrdered(Short dataId, Segment orderedPackage) {
                if (orderedPackage != null)
                    orderedSegments.add(orderedPackage);
            }
        };

        Segment segment = new Segment(input, serializeShort(input));
        dataMap.put(segment);
        Deencapsulation.invoke(handler, "removeFromTail");


        Short actualNextRemoteSeq = Deencapsulation.getField(handler, "nextDataId");
        assertEquals("Next remote sequence must match", nextRemoteSeq, actualNextRemoteSeq);

        if (outputs.length != 0)
            assertEquals("Ordered segment count must match", outputs.length, orderedSegments.size());
        else
            assertEquals("No ordered segment must have occured", 0, orderedSegments.size());

        int i = 0;
        for (Segment orderedSegment : orderedSegments) {
            assertEquals("Order and contents of datas must match", (short) outputs[i], deserializeShort(orderedSegment.getData()));
            i++;
        }
    }

    public Object[][] parametersForTestPut() {
        Object[][] out = (Object[][])
                $($(
                        (short) -3, false
                ), $(
                        (short) 1, true
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testPut(Short ref, boolean addedRef) {
        dataId = 0;
        Deencapsulation.setField(handler, "nextDataId", (short) 1);
        dataMap.clear();

        Segment segment = new Segment(ref, serializeShort(ref));
        dataMap.put(segment);
        if (addedRef)
            assertEquals("Ref was added as expected", segment, dataMap.getValue(ref));
        else
            assertNull("Ref was not added as expected", dataMap.getValue(ref));

        dataMap.clear();
    }

}
