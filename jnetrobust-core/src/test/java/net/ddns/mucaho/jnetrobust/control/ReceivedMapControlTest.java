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
    protected static ReceivedMapControl handler = new ReceivedMapControl((short) 0, config.listener,
            config.packetQueueLimit, config.packetQueueTimeout);

    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    @BeforeClass
    public static void clearMap() {
        initDataMap(handler);
        dataMap.clear();
    }

    public Object[][] parametersForTestRemoveTail() {
        Object[][] out = (Object[][])
                $($(
                        $S(3), $($S), (short) 1
                ), $(
                        $S(2, 5), $($S), (short) 1
                ), $(
                        $S(1), $($S(1), $S(2, 5), $S(3)), (short) 4
                ), $(
                        $S(4), $($S(4)), (short) 6
                ), $(
                        $S(7, 9), $($S), (short) 6
                ), $(
                        $S(8, 10), $($S), (short) 6
                ), $(
                        $S(6, 11), $($S(6, 11), $S(7, 9), $S(8, 10)), (short) 12
                ), $(
                        $S(1, 2, 3), $($S), (short) 12
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testRemoveTail(final Short[] inputs, final Short[][] outputs,
                                     final Short nextRemoteSeq) {

        final LinkedHashSet<MultiKeyValue> orderedDatas = new LinkedHashSet<MultiKeyValue>();
        new MockUp<ReceivedMapControl>() {
            @SuppressWarnings("unused")
            @Mock
            private void notifyOrdered(MultiKeyValue orderedPackage) {
                if (orderedPackage != null)
                    orderedDatas.add(orderedPackage);
            }
        };

        MultiKeyValue data = new MultiKeyValue(++dataId, inputs);
        for (Short ref : inputs) {
            dataMap.put(ref, null);
        }
        dataMap.put(inputs[0], data);
        Deencapsulation.invoke(handler, "removeTail");


        Short actualNextRemoteSeq = (Short) Deencapsulation.getField(handler, "nextRemoteSeq");
        assertEquals("Next remote sequence must match", nextRemoteSeq, actualNextRemoteSeq);

        if (outputs[0].length != 0)
            assertEquals("Ordered data count must match", outputs.length, orderedDatas.size());
        else
            assertEquals("No ordered data must have occured", 0, orderedDatas.size());

        int i = 0;
        for (MultiKeyValue orderedData : orderedDatas) {
            assertArrayEquals("Order and contents of datas must match",
                    outputs[i], (Short[]) orderedData.getValue());
            i++;
        }
    }

    @SuppressWarnings("unchecked")
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
        Deencapsulation.setField(handler, "nextRemoteSeq", (short) 1);
        dataMap.clear();

        MultiKeyValue data = new MultiKeyValue(++dataId, ref);
        Deencapsulation.invoke(data, "addDynamicReference", ref);

        dataMap.put(ref, data);
        if (addedRef)
            assertEquals("Ref was added as expected", data, dataMap.get(ref));
        else
            assertNull("Ref was not added as expected", dataMap.get(ref));
    }

}
