package net.ddns.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.ddns.mucaho.jnetrobust.util.EntryIterator;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class MetadataMapIterTest {
    private static short dataId = Short.MIN_VALUE;

    public Object[][] parametersForTestIterator() {
        Object[][] out = (Object[][])
                $($(
                        $($S(1, 2, 3), $S(4, 5), $S(7, 9), $S(8, 10))
                ));
        return out;
    }

    @Test
    @Parameters
    public final void testIterator(Short[][] refGroups) {
        MetadataMap<Object> dataMap = new MetadataMap<Object>(SequenceComparator.instance);
        EntryIterator<Short, Metadata<Object>> iter = dataMap.getIterator();
        List<Short> refs = new ArrayList<Short>();
        HashSet<Metadata<Object>> metadatas = new HashSet<Metadata<Object>>();
        Metadata<Object> metadata;
        int expectedCount, actualCount;
        Short key;


        expectedCount = 0;
        for (final Short[] refGroup : refGroups) {
            expectedCount += refGroup.length;
            metadata = new Metadata<Object>(++dataId, refGroup);
            dataMap.putAll(new TreeSet<Short>(Arrays.asList(refGroup)), metadata);
            metadatas.add(metadata);
            refs.addAll(Arrays.asList(refGroup));
        }

        actualCount = 0;
        key = iter.getHigherKey(null);
        while (key != null) {
            actualCount++;
            metadatas.remove(iter.getValue(key));
            refs.remove(key);
            key = iter.getHigherKey(key);
        }
        assertEquals("Iterator didn't iterate over all datas!", expectedCount, actualCount);
        assertEquals("All refs should have been removed.", 0, refs.size());
        assertEquals("All metadatas should have been removed.", 0, metadatas.size());


        actualCount = 0;
        key = iter.getHigherKey(null);
        while (key != null) {
            actualCount++;
            iter.removeValue(key);
            key = iter.getHigherKey(key);
        }
        assertEquals("Iterator didn't remove all datas.", refGroups.length, actualCount);
        assertEquals("DataMap should be empty.", 0, dataMap.size());
    }
}
