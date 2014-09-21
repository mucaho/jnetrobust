package net.ddns.mucaho.jnetrobust.controller;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
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
        Packet inPkg, outPkg;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outPkg = new Packet();
            outPkg.setAck(ack);
            outPkg.setLastAcks(lastAcks);
            MultiKeyValue data = new MultiKeyValue();
            data.setStaticReference(Short.MIN_VALUE);
            data.addDynamicReference(Short.MIN_VALUE);
            outPkg.setData(data);
            outPkg.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inPkg = new Packet();
            inPkg.readExternal(in);
        }
        in.close();

        assertEquals("ack mismatch", outPkg.getAck(), inPkg.getAck());
        assertEquals("lastAck mismatch", outPkg.getLastAcks(), inPkg.getLastAcks());
        assertNotSame("datas are different objects", outPkg.getData(), inPkg.getData());
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
        MultiKeyValue data = new MultiKeyValue();
        data.setStaticReference(Short.MIN_VALUE);
        data.addDynamicReference(Short.MIN_VALUE);
        original.setData(data);
        Packet clone = (Packet) original.clone();


        assertEquals("ack mismatch", original.getAck(), clone.getAck());
        assertEquals("lastAck mismatch", original.getLastAcks(), clone.getLastAcks());

        assertNotSame("datas are different objects", original.getData(), clone.getData());

        original.setAck((short) -1);
        original.setLastAcks(-1);
        assertEquals("Cloned ack did not change", ack.shortValue(), clone.getAck());
        assertEquals("Cloned ack did not change", lastAcks.intValue(), clone.getLastAcks());
    }
}
