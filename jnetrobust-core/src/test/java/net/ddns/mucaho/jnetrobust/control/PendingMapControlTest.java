package net.ddns.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static net.ddns.mucaho.jnetrobust.util.TestUtils.binInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class PendingMapControlTest extends MapControlTest {

    protected PendingMapControl handler = new PendingMapControl(config.listener, config.getPacketQueueLimit(),
            config.getPacketOffsetLimit(), config.getPacketRetransmitLimit() + 1, config.getPacketQueueTimeout());

    public PendingMapControlTest() {
        initDataMap(handler);
    }

    @Before
    public final void prepareTest() {
        dataMap.clear();
    }

    public Object[][] parametersForTestRemoveFromPending() {
        // MultiRefObject:
        // * it is important what the first reference is (first short in each line)
        // * order of insertion must be equal to order of removal for testing purposes (order of lines)

        Object[][] out = (Object[][])
                $($(
                        (short) 10, binInt("0"), $($S(10))
                ), $(
                        (short) 10, binInt("1"), $($S(9))
                ), $(
                        (short) 10, binInt("1111111111111"), $($S(10))
                ), $(
                        (short) 10, binInt("1111111111111"), $($S(-1))
                ), $(
                        (short) 10, binInt("1"), $($S(9, 10))
                ), $(
                        (short) 10, binInt("101"), $($S(7, 8, 9, 10))
                ), $(
                        (short) 10, binInt("101"), $($S(7), $S(9))
                ), $(
                        (short) 10, binInt("100"), $($S(7), $S(10))
                ), $(
                        (short) 10, binInt("101"), $($S(7), $S(9), $S(10))
                ), $(
                        (short) 10, binInt("101"), $($S(7, 21), $S(9, 19), $S(10, 20))
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testRemoveFromPending(short localSeq, int lastLocalSeqs,
                                            final Short[][] referenceGroups) {


        class Wrapper {
            public int invocations = 0;
        }
        final Wrapper wrapper = new Wrapper();
        new MockUp<PendingMapControl>() {
            @Mock
            @SuppressWarnings("unused")
            protected void notifyAcked(Invocation invocation, MetadataUnit ackedMetadata, boolean directlyAcked) {
                if (ackedMetadata != null) {
                    assertEquals("Expected other value (insertion order must be remove order)",
                            wrapper.invocations, ackedMetadata.getValue());
                    wrapper.invocations++;
                }
            }
        };

        for (int i = 0; i < referenceGroups.length; ++i) {
            addData(i, referenceGroups[i]);
        }
        handler.removeFromPending(localSeq, lastLocalSeqs);

        assertEquals("Invocation count should match data count.", wrapper.invocations, referenceGroups.length);
        assertTrue("DataMap should be empty.", dataMap.isEmpty());
    }

    public Object[][] parametersForTestAddToPending() {
        Object[][] out = (Object[][])
                $($(
                        $($S(10))
                ), $(
                        $($S(7, 8, 9, 10))
                ), $(
                        $($S(7, 21), $S(9, 19), $S(10, 20))
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testAddToPending(final Short[][] referenceGroups) {
        for (Short[] referenceGroup : referenceGroups) {
            MetadataUnit metadata = new MetadataUnit(++dataId, referenceGroup);
            for (Short reference : referenceGroup) {
                handler.addToPending(reference, metadata);
            }
        }

        int dataCount = 0;
        int referenceCount = 0;
        for (Short[] referenceGroup : referenceGroups) {
            dataCount++;
            referenceCount += referenceGroup.length;
        }
        assertEquals("Total data count match", dataCount,
                new HashSet<MetadataUnit>(handler.dataMap.getMap().values()).size());
        assertEquals("Total reference count match", referenceCount, handler.dataMap.getMap().keySet().size());


        for (MetadataUnit metadata : handler.dataMap.getMap().values()) {
            Short[] dataValues = (Short[]) metadata.getValue();
            assertEquals("Reference count match", dataValues.length, metadata.getDynamicReferences().size());
            for (Short dataValue : dataValues) {
                assertTrue("Reference match", metadata.getDynamicReferences().contains(dataValue));
            }
        }

    }

}
