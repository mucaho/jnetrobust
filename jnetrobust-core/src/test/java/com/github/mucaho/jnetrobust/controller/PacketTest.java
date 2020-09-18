/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.control.Segment;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

@RunWith(JUnitParamsRunner.class)
public class PacketTest {
    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public final void testExceptions() {
        Packet packet = new Packet();
        for (int i = 0; i <= Packet.MAX_DATAS_PER_PACKET; ++i) {
            packet.addLastSegment(new Segment());
        }
    }

    public Object[][] parametersForTestSerialization() {
        Object[][] out = (Object[][])
                $($(
                        (short) 10, 1831
                ), $(
                        (short) 20, 1245
                ));

        return out;
    }


    @Test
    @Parameters
    public final void testSerialization(Short ack, Integer lastAcks) throws Exception {
        Packet inPacket, outPacket;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outPacket = new Packet();
            outPacket.setTransmissionAck(ack);
            outPacket.setPrecedingTransmissionAcks(lastAcks);
            Segment segment = new Segment();
            Deencapsulation.setField(segment, "dataId", Short.MIN_VALUE);
            Deencapsulation.invoke(segment, "addTransmissionId", Short.MIN_VALUE);
            outPacket.addLastSegment(segment);
            outPacket.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inPacket = new Packet();
            inPacket.readExternal(in);
        }
        in.close();

        assertEquals("ack mismatch", outPacket.getTransmissionAck(), inPacket.getTransmissionAck());
        assertEquals("lastAck mismatch", outPacket.getPrecedingTransmissionAcks(), inPacket.getPrecedingTransmissionAcks());
        assertNotSame("datas are different objects", outPacket.getFirstSegment(), inPacket.getFirstSegment());
    }


    public Object[][] parametersForTestClone() {
        Object[][] out = (Object[][])
                $($(
                        (short) 10, 1831
                ), $(
                        (short) 20, 1245
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testClone(Short ack, Integer lastAcks) throws Exception {
        Packet original = new Packet();
        original.setTransmissionAck(ack);
        original.setPrecedingTransmissionAcks(lastAcks);

        Segment segment = new Segment();
        Deencapsulation.setField(segment, "dataId", Short.MIN_VALUE);
        Deencapsulation.invoke(segment, "addTransmissionId", Short.MIN_VALUE);
        original.addLastSegment(segment);
        Packet clone = original.clone();

        assertEquals("ack mismatch", original.getTransmissionAck(), clone.getTransmissionAck());
        assertEquals("lastAck mismatch", original.getPrecedingTransmissionAcks(), clone.getPrecedingTransmissionAcks());
        assertNotSame("datas are different objects", original.getFirstSegment(), clone.getFirstSegment());

        original.setTransmissionAck((short) -1);
        original.setPrecedingTransmissionAcks(-1);
        original.addLastSegment(new Segment());
        assertEquals("Original ack did change", (short) -1, original.getTransmissionAck().shortValue());
        assertEquals("Original lastAck did change", -1, original.getPrecedingTransmissionAcks());
        assertEquals("Original segments size did change", 2, original.getSegments().size());
        assertEquals("Cloned ack did not change", ack.shortValue(), clone.getTransmissionAck().shortValue());
        assertEquals("Cloned lastAck did not change", lastAcks.intValue(), clone.getPrecedingTransmissionAcks());
        assertEquals("Cloned datas size did not change", 1, clone.getSegments().size());
    }
}
