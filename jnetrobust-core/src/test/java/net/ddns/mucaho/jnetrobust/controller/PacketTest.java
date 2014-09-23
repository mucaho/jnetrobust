package net.ddns.mucaho.jnetrobust.controller;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.control.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
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
            packet.addLastMetadata(new Metadata());
        }
    }

    @SuppressWarnings("unchecked")
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
            outPacket.setAck(ack);
            outPacket.setLastAcks(lastAcks);
            Metadata metadata = new Metadata();
            Deencapsulation.setField(metadata, "staticReference", Short.MIN_VALUE);
            Deencapsulation.invoke(metadata, "addDynamicReference", Short.MIN_VALUE);
            outPacket.addLastMetadata(metadata);
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

        assertEquals("ack mismatch", outPacket.getAck(), inPacket.getAck());
        assertEquals("lastAck mismatch", outPacket.getLastAcks(), inPacket.getLastAcks());
        assertNotSame("datas are different objects", outPacket.getFirstMetadata(), inPacket.getFirstMetadata());
    }


    @SuppressWarnings("unchecked")
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
        original.setAck(ack);
        original.setLastAcks(lastAcks);

        Metadata metadata = new Metadata();
        Deencapsulation.setField(metadata, "staticReference", Short.MIN_VALUE);
        Deencapsulation.invoke(metadata, "addDynamicReference", Short.MIN_VALUE);
        original.addLastMetadata(metadata);
        Packet clone = (Packet) original.clone();

        assertEquals("ack mismatch", original.getAck(), clone.getAck());
        assertEquals("lastAck mismatch", original.getLastAcks(), clone.getLastAcks());
        assertNotSame("datas are different objects", original.getFirstMetadata(), clone.getFirstMetadata());

        original.setAck((short) -1);
        original.setLastAcks(-1);
        original.addLastMetadata(new Metadata());
        assertEquals("Cloned ack did not change", ack.shortValue(), clone.getAck());
        assertEquals("Cloned lastAck did not change", lastAcks.intValue(), clone.getLastAcks());
        assertEquals("Cloned datas size did not changte", 1, clone.getMetadatas().size());
    }
}
