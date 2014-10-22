/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.controller.Packet;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.control.Metadata;
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
        Packet<Object> packet = new Packet<Object>();
        for (int i = 0; i <= Packet.MAX_DATAS_PER_PACKET; ++i) {
            packet.addLastMetadata(new Metadata<Object>());
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
        Packet<Object> inPacket, outPacket;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outPacket = new Packet<Object>();
            outPacket.setTransmissionAck(ack);
            outPacket.setPrecedingTransmissionAcks(lastAcks);
            Metadata<Object> metadata = new Metadata<Object>();
            Deencapsulation.setField(metadata, "dataId", Short.MIN_VALUE);
            Deencapsulation.invoke(metadata, "addTransmissionId", Short.MIN_VALUE);
            outPacket.addLastMetadata(metadata);
            outPacket.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inPacket = new Packet<Object>();
            inPacket.readExternal(in);
        }
        in.close();

        assertEquals("ack mismatch", outPacket.getTransmissionAck(), inPacket.getTransmissionAck());
        assertEquals("lastAck mismatch", outPacket.getPrecedingTransmissionAcks(), inPacket.getPrecedingTransmissionAcks());
        assertNotSame("datas are different objects", outPacket.getFirstMetadata(), inPacket.getFirstMetadata());
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
        Packet<Object> original = new Packet<Object>();
        original.setTransmissionAck(ack);
        original.setPrecedingTransmissionAcks(lastAcks);

        Metadata<Object> metadata = new Metadata<Object>();
        Deencapsulation.setField(metadata, "dataId", Short.MIN_VALUE);
        Deencapsulation.invoke(metadata, "addTransmissionId", Short.MIN_VALUE);
        original.addLastMetadata(metadata);
        Packet<Object> clone = original.clone();

        assertEquals("ack mismatch", original.getTransmissionAck(), clone.getTransmissionAck());
        assertEquals("lastAck mismatch", original.getPrecedingTransmissionAcks(), clone.getPrecedingTransmissionAcks());
        assertNotSame("datas are different objects", original.getFirstMetadata(), clone.getFirstMetadata());

        original.setTransmissionAck((short) -1);
        original.setPrecedingTransmissionAcks(-1);
        original.addLastMetadata(new Metadata<Object>());
        assertEquals("Cloned ack did not change", ack.shortValue(), clone.getTransmissionAck());
        assertEquals("Cloned lastAck did not change", lastAcks.intValue(), clone.getPrecedingTransmissionAcks());
        assertEquals("Cloned datas size did not changte", 1, clone.getMetadatas().size());
    }
}
