package net.ddns.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class MultiKeyValueTest {
    private final static MultiKeyValue data = new MultiKeyValue();

    public Object[][] parametersForTestReferences() {
        Object[][] out = (Object[][])
                $($(
                        true, $S(2), $S(2)
                ), $(
                        false, $S(2), $S
                ), $(
                        true, $S(2, 2), $S(2)
                ), $(
                        false, $S(2, 2), $S
                ), $(
                        true, $S(5, 3, 4), $S(3, 4, 5)
                ), $(
                        false, $S(4), $S(3, 5)
                ));

        return out;

    }

    @Test
    @Parameters
    public final void testReferences(boolean shouldAdd, Short[] inRefs, Short[] expectedRefs) {
        if (shouldAdd) {
            for (short ref : inRefs)
                data.addDynamicReference(ref);
        } else {
            for (short ref : inRefs)
                data.removeDynamicReference(ref);
        }

        assertEquals("Reference count mismatch.",
                expectedRefs.length, data.getDynamicReferences().size());

        int i = 0;
        for (short ref : data.getDynamicReferences()) {
            assertEquals("Reference does not match.", expectedRefs[i++], (Short) ref);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public final void testExceptions() {
        MultiKeyValue data = new MultiKeyValue();
        Integer max = Deencapsulation.getField(MultiKeyValue.class, "MAX_CAPACITY");
        for (int i = 0; i <= max; ++i) {
            data.addDynamicReference((short) i);
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
        MultiKeyValue inData, outData;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outData = new MultiKeyValue(refs[0], value);
            for (Short ref : refs)
                outData.addDynamicReference(ref);
            outData.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inData = new MultiKeyValue();
            inData.readExternal(in);
        }
        in.close();

        assertEquals("Value mismatch.", outData.getValue(), inData.getValue());
        assertEquals("Static reference mismatch", outData.getStaticReference(), inData.getStaticReference());
        assertEquals("Reference count mismatch.", 1, inData.getDynamicReferences().size());
        assertEquals("Dynamic reference mismatch", outData.getLastDynamicReference(), inData.getLastDynamicReference());
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
        MultiKeyValue original = new MultiKeyValue(refs[0], value);
        for (Short ref : refs)
            original.addDynamicReference(ref);
        MultiKeyValue clone = (MultiKeyValue) original.clone();

        assertEquals("Value mismatch.", original.getValue(), clone.getValue());
        assertEquals("Value mismatch", original.getStaticReference(), clone.getStaticReference());
        assertArrayEquals("References mismatch",
                original.getDynamicReferences().toArray(), clone.getDynamicReferences().toArray());

        assertSame("Value has to be same.",
                Deencapsulation.getField(original, "value"),
                Deencapsulation.getField(clone, "value"));
        assertNotSame("Reference has to differ",
                Deencapsulation.getField(original, "staticReference"),
                Deencapsulation.getField(clone, "staticReference"));
        assertNotSame("References have to differ.",
                Deencapsulation.getField(original, "dynamicReferences"),
                Deencapsulation.getField(clone, "dynamicReferences"));

        original.setStaticReference(Short.MIN_VALUE);
        assertFalse("Cloned reference did not change", original.getStaticReference().equals(clone.getStaticReference()));

        original.addDynamicReference((short) -1);
        assertEquals("Cloned reference count did not change", refs.length, clone.getDynamicReferences().size());

    }
}
