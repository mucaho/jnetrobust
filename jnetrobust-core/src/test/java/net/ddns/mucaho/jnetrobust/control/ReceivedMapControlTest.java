/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashSet;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class ReceivedMapControlTest extends MapControlTest {
    protected static ReceivedMapControl<Object> handler = new ReceivedMapControl<Object>((short) 0, config.listener,
            config.getPacketQueueLimit(), config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1,
            config.getPacketQueueTimeout());

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

        final LinkedHashSet<Metadata<Object>> orderedMetadatas = new LinkedHashSet<Metadata<Object>>();
        new MockUp<ReceivedMapControl<Object>>() {
            @SuppressWarnings("unused")
            @Mock
            private void notifyOrdered(Metadata<Object> orderedPackage) {
                if (orderedPackage != null)
                    orderedMetadatas.add(orderedPackage);
            }
        };

        Metadata<Object> metadata = new Metadata<Object>(input, input);
        dataMap.putDataId(metadata);
        Deencapsulation.invoke(handler, "removeTail");


        Short actualNextRemoteSeq = Deencapsulation.getField(handler, "nextDataId");
        assertEquals("Next remote sequence must match", nextRemoteSeq, actualNextRemoteSeq);

        if (outputs.length != 0)
            assertEquals("Ordered metadata count must match", outputs.length, orderedMetadatas.size());
        else
            assertEquals("No ordered metadata must have occured", 0, orderedMetadatas.size());

        int i = 0;
        for (Metadata<Object> orderedMetadata : orderedMetadatas) {
            assertEquals("Order and contents of datas must match", outputs[i], orderedMetadata.getData());
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

        Metadata<Object> metadata = new Metadata<Object>(ref, ref);
        dataMap.putDataId(metadata);
        if (addedRef)
            assertEquals("Ref was added as expected", metadata, dataMap.get(ref));
        else
            assertNull("Ref was not added as expected", dataMap.get(ref));

        dataMap.clear();
    }

}
