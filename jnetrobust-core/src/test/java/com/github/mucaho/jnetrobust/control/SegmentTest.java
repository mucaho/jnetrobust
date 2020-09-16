/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeSet;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class SegmentTest {
    public Object[][] parametersForTestReferences() {
        Segment<Object> segment2 = new Segment<Object>();
        segment2.addTransmissionId((short)2);

        Segment<Object> segment22 = new Segment<Object>();
        segment22.addTransmissionId((short)2);
        segment22.addTransmissionId((short)2);

        Segment<Object> segment345 = new Segment<Object>();
        segment345.addTransmissionId((short)3);
        segment345.addTransmissionId((short)4);
        segment345.addTransmissionId((short)5);

        Object[][] out = (Object[][])
                $($(
                        new Segment<Object>(), true, $S(2), $S(2)
                ), $(
                        segment2, false, $S(2), $S
                ), $(
                        new Segment<Object>(), true, $S(2, 2), $S(2)
                ), $(
                        segment22, false, $S(2, 2), $S
                ), $(
                        new Segment<Object>(), true, $S(5, 3, 4), $S(3, 4, 5)
                ), $(
                        segment345, false, $S(4), $S(3, 5)
                ));

        return out;

    }

    @Test
    @Parameters
    public final void testReferences(Segment<Object> segment, boolean shouldAdd,
                                     Short[] inRefs, Short[] expectedRefs) {
        if (shouldAdd) {
            for (short ref : inRefs)
                segment.addTransmissionId(ref);
        } else {
            for (short ref : inRefs)
                segment.removeTransmissionId(ref);
        }

        assertEquals("Reference count mismatch.",
                expectedRefs.length, segment.getTransmissionIds().size());

        int i = 0;
        for (short ref : segment.getTransmissionIds()) {
            assertEquals("Reference does not match.", expectedRefs[i++], new Short(ref));
        }
    }

    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    public Object[][] parametersForTestSerialization() {
        Object[][] out = (Object[][])
                $($(
                        $S(1, 2, 3), "Heyya!"
                ), $(
                        $S(3, 2), "Heyya!"
                ));

        return out;
    }


    @Test
    @Parameters
    public final void testSerialization(Short[] refs, String value) throws Exception {
        Segment<Object> inSegment, outSegment;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outSegment = new Segment<Object>(refs[0], value);
            for (Short ref : refs)
                outSegment.addTransmissionId(ref);
            outSegment.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inSegment = new Segment<Object>();
            inSegment.readExternal(in);
        }
        in.close();

        assertEquals("Value mismatch.", outSegment.getData(), inSegment.getData());
        assertEquals("Static reference mismatch", outSegment.getDataId(), inSegment.getDataId());
        assertEquals("Reference count mismatch.", 1, inSegment.getTransmissionIds().size());
        assertEquals("TransmissionId mismatch", outSegment.getLastTransmissionId(), inSegment.getLastTransmissionId());
    }


    public Object[][] parametersForTestClone() {
        Object[][] out = (Object[][])
                $($(
                        $S(1, 2, 3), "Heyya!"
                ), $(
                        $S(3, 2), "Heyya!"
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testClone(Short[] refs, String value) throws Exception {
        Segment<Object> original = new Segment<Object>(refs[0], value);
        for (Short ref : refs)
            original.addTransmissionId(ref);
        Segment<Object> clone = original.clone();

        assertEquals("Value mismatch.", original.getData(), clone.getData());
        assertEquals("Value mismatch", original.getDataId(), clone.getDataId());
        assertArrayEquals("References mismatch",
                original.getTransmissionIds().toArray(), clone.getTransmissionIds().toArray());

        assertSame("Value has to be same.",
                Deencapsulation.getField(original, "value"),
                Deencapsulation.getField(clone, "value"));
        assertNotSame("Reference has to differ",
                Deencapsulation.getField(original, "dataId"),
                Deencapsulation.getField(clone, "dataId"));
        assertNotSame("References have to differ.",
                Deencapsulation.getField(original, "transmissionIds"),
                Deencapsulation.getField(clone, "transmissionIds"));
        /*
        Short[] originalRefArray = original.getTransmissionIds().toArray(new Short[0]);
        Short[] clonedRefArray = clone.getTransmissionIds().toArray(new Short[0]);
        for (int i = 0; i < originalRefArray.length; ++i)
            assertNotSame("Reference has to differ", originalRefArray[i], clonedRefArray[i]);
        */

        Deencapsulation.setField(original, "value", "__NEW_DATA__");
        assertEquals("Original value did change", "__NEW_DATA__", original.getData());
        assertNotEquals("Cloned value did not change", original.getData().equals(clone.getData()));

        Deencapsulation.setField(original, "dataId", Short.MIN_VALUE);
        assertEquals("Original dataId did change", new Short(Short.MIN_VALUE), original.getDataId());
        assertNotEquals("Cloned reference did not change", original.getDataId().equals(clone.getDataId()));

        original.addTransmissionId((short) -1);
        assertEquals("Original reference count did change", refs.length + 1, original.getTransmissionIds().size());
        assertTrue("Original reference was added", original.getTransmissionIds().contains((short)-1));
        assertEquals("Cloned reference count did not change", refs.length, clone.getTransmissionIds().size());
        NavigableSet<Short> sortedRefs = new TreeSet<Short>(Arrays.asList(refs));
        assertArrayEquals("Cloned references did not change", sortedRefs.toArray(), clone.getTransmissionIds().toArray());
    }

    @Test
    public void testImmutableCollectionsReturned() {
        Segment<Object> segment2 = new Segment<Object>();
        segment2.addTransmissionId((short)2);

        try {
            segment2.getTransmissionIds().remove((short)2);
            fail("Returned collection should be immutable");
        } catch (UnsupportedOperationException e) {
        }

        try {
            segment2.getDataIds().remove((short)2);
            fail("Returned collection should be immutable");
        } catch (UnsupportedOperationException e) {
        }
    }
}