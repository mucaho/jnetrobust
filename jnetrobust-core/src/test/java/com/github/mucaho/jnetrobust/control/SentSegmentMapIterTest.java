/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import com.github.mucaho.jnetrobust.util.EntryIterator;
import com.github.mucaho.jnetrobust.util.IdComparator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$;
import static com.github.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class SentSegmentMapIterTest {
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
        AbstractSegmentMap<Object> dataMap = new SentSegmentMap<Object>();
        EntryIterator<Short, Segment<Object>> iter = dataMap.getIterator();
        List<Short> refs = new ArrayList<Short>();
        HashSet<Segment<Object>> segments = new HashSet<Segment<Object>>();
        Segment<Object> segment;
        int expectedCount, actualCount;
        Short key;


        expectedCount = 0;
        for (final Short[] refGroup : refGroups) {
            expectedCount += refGroup.length;
            segment = new Segment<Object>(++dataId, refGroup);
            dataMap.putAll(new TreeSet<Short>(Arrays.asList(refGroup)), segment);
            segments.add(segment);
            refs.addAll(Arrays.asList(refGroup));
        }

        actualCount = 0;
        key = iter.getHigherKey(null);
        while (key != null) {
            actualCount++;
            segments.remove(iter.getValue(key));
            refs.remove(key);
            key = iter.getHigherKey(key);
        }
        assertEquals("Iterator didn't iterate over all datas!", expectedCount, actualCount);
        assertEquals("All refs should have been removed.", 0, refs.size());
        assertEquals("All segments should have been removed.", 0, segments.size());


        actualCount = 0;
        key = iter.getHigherKey(null);
        while (key != null) {
            actualCount++;
            iter.removeValue(key);
            key = iter.getHigherKey(key);
        }
        assertEquals("Iterator didn't remove all datas.", refGroups.length, actualCount);
        assertEquals("DataMap should be empty.", 0, dataMap.keySize());
    }
}
