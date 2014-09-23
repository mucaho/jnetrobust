package net.ddns.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import org.junit.Ignore;
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
public class MetadataTest {
    public Object[][] parametersForTestReferences() {
        Metadata<Object> metadata = new Metadata<Object>();
        Object[][] out = (Object[][])
                $($(
                        metadata, true, $S(2), $S(2)
                ), $(
                        metadata, false, $S(2), $S
                ), $(
                        metadata, true, $S(2, 2), $S(2)
                ), $(
                        metadata, false, $S(2, 2), $S
                ), $(
                        metadata, true, $S(5, 3, 4), $S(3, 4, 5)
                ), $(
                        metadata, false, $S(4), $S(3, 5)
                ));

        return out;

    }

    @Test
    @Parameters
    @Ignore //FIXME unignore this and figure out why Maven test is not succeeding
    public final void testReferences(Metadata<Object> metadata, boolean shouldAdd,
                                     Short[] inRefs, Short[] expectedRefs) {
        if (shouldAdd) {
            for (short ref : inRefs)
                metadata.addDynamicReference(ref);
        } else {
            for (short ref : inRefs)
                metadata.removeDynamicReference(ref);
        }

        assertEquals("Reference count mismatch.",
                expectedRefs.length, metadata.getDynamicReferences().size());

        int i = 0;
        for (short ref : metadata.getDynamicReferences()) {
            assertEquals("Reference does not match.", expectedRefs[i++], (Short) ref);
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
        Metadata<Object> inMetadata, outMetadata;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        {
            outMetadata = new Metadata<Object>(refs[0], value);
            for (Short ref : refs)
                outMetadata.addDynamicReference(ref);
            outMetadata.writeExternal(out);
        }
        out.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inStream);
        {
            inMetadata = new Metadata<Object>();
            inMetadata.readExternal(in);
        }
        in.close();

        assertEquals("Value mismatch.", outMetadata.getData(), inMetadata.getData());
        assertEquals("Static reference mismatch", outMetadata.getStaticReference(), inMetadata.getStaticReference());
        assertEquals("Reference count mismatch.", 1, inMetadata.getDynamicReferences().size());
        assertEquals("Dynamic reference mismatch", outMetadata.getLastDynamicReference(), inMetadata.getLastDynamicReference());
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
        Metadata<Object> original = new Metadata<Object>(refs[0], value);
        for (Short ref : refs)
            original.addDynamicReference(ref);
        Metadata<Object> clone = original.clone();

        assertEquals("Value mismatch.", original.getData(), clone.getData());
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

        Deencapsulation.setField(original, "staticReference", Short.MIN_VALUE);
        assertFalse("Cloned reference did not change", original.getStaticReference().equals(clone.getStaticReference()));

        original.addDynamicReference((short) -1);
        assertEquals("Cloned reference count did not change", refs.length, clone.getDynamicReferences().size());

    }
}
