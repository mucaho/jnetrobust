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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeSet;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class SegmentTest {
    public Object[][] parametersForTestReferences() {
        Segment segment2 = new Segment();
        segment2.addTransmissionId((short)2);

        Segment segment22 = new Segment();
        segment22.addTransmissionId((short)2);
        segment22.addTransmissionId((short)2);

        Segment segment345 = new Segment();
        segment345.addTransmissionId((short)3);
        segment345.addTransmissionId((short)4);
        segment345.addTransmissionId((short)5);

        Object[][] out = (Object[][])
                $($(
                        new Segment(), true, $S(2), $S(2)
                ), $(
                        segment2, false, $S(2), $S
                ), $(
                        new Segment(), true, $S(2, 2), $S(2)
                ), $(
                        segment22, false, $S(2, 2), $S
                ), $(
                        new Segment(), true, $S(5, 3, 4), $S(3, 4, 5)
                ), $(
                        segment345, false, $S(4), $S(3, 5)
                ));

        return out;

    }

    @Test
    @Parameters
    public final void testReferences(Segment segment, boolean shouldAdd,
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
        Segment inSegment, outSegment;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outSegment = new Segment(refs[0], serialize(value));
            for (Short ref : refs)
                outSegment.addTransmissionId(ref);
            outSegment.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inSegment = new Segment();
            inSegment.readExternal(in);
        }
        in.close();

        assertEquals("Value mismatch.", deserialize(outSegment.getData()), deserialize(inSegment.getData()));
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
        Segment original = new Segment(refs[0], serialize(value));
        for (Short ref : refs)
            original.addTransmissionId(ref);
        Segment clone = original.clone();

        assertEquals("Value mismatch.", deserialize(original.getData()), deserialize(clone.getData()));
        assertEquals("Value mismatch", original.getDataId(), clone.getDataId());
        assertArrayEquals("References mismatch",
                original.getTransmissionIds().toArray(), clone.getTransmissionIds().toArray());

        assertNotSame("References have to differ.",
                Deencapsulation.getField(original, "data"),
                Deencapsulation.getField(clone, "data"));
        assertSame("Objects have to be same",
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

        Deencapsulation.setField(original, "data", serialize("__NEW_DATA__"));
        Deencapsulation.setField(original, "dataOut", serialize("__NEW_DATA__"));
        assertEquals("Original value did change", "__NEW_DATA__", deserialize(original.getData()));
        assertNotEquals("Cloned value did not change", deserialize(original.getData()), deserialize(clone.getData()));

        Deencapsulation.setField(original, "dataId", Short.MIN_VALUE);
        assertEquals("Original dataId did change", new Short(Short.MIN_VALUE), original.getDataId());
        assertNotEquals("Cloned reference did not change", original.getDataId(), clone.getDataId());

        original.addTransmissionId((short) -1);
        assertEquals("Original reference count did change", refs.length + 1, original.getTransmissionIds().size());
        assertTrue("Original reference was added", original.getTransmissionIds().contains((short)-1));
        assertEquals("Cloned reference count did not change", refs.length, clone.getTransmissionIds().size());
        NavigableSet<Short> sortedRefs = new TreeSet<Short>(Arrays.asList(refs));
        assertArrayEquals("Cloned references did not change", sortedRefs.toArray(), clone.getTransmissionIds().toArray());
    }

    @Test
    public void testImmutableCollectionsReturned() {
        Segment segment2 = new Segment();
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

    protected static ByteBuffer serialize(String value) {
        char[] chars = value.toCharArray();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE + chars.length * Character.SIZE / Byte.SIZE);
        buffer.putInt(chars.length);
        for (char chr : chars) buffer.putChar(chr);
        buffer.flip();
        return buffer;
    }

    protected static String deserialize(ByteBuffer data) {
        int size = data.getInt();
        char[] chars = new char[size];
        for (int i = 0; i < size; ++i) {
            chars[i] = data.getChar();
        }
        return String.valueOf(chars);
    }
}
