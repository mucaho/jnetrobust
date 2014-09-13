package net.ddns.mucaho.jnetrobust.data;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValueMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import net.ddns.mucaho.jnetrobust.util.EntryIterator;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

@RunWith(JUnitParamsRunner.class)
public class MultiKeyValueMapIterTest {
	private static short dataId = Short.MIN_VALUE;

	public Object[][] parametersForTestIterator() {
		Object[][] out = (Object[][]) 
			$($(
				$($S(1,2,3), $S(4,5), $S(7,9), $S(8,10))
			));
		return out;
	}
	@Test
	@Parameters
	public final void testIterator(Short[][] refGroups) {
		MultiKeyValueMap dataMap = new MultiKeyValueMap(SequenceComparator.instance);
		EntryIterator<Short, MultiKeyValue> iter = dataMap.getIterator();
		List<Short> refs = new ArrayList<Short>();
		HashSet<MultiKeyValue> datas = new HashSet<MultiKeyValue>();
		MultiKeyValue data;
		int expectedCount, actualCount;
		Short key;

		
		expectedCount = 0;
		for (final Short[] refGroup: refGroups) {
			expectedCount += refGroup.length;
			data = new MultiKeyValue(++dataId, refGroup);
			dataMap.putAll(new TreeSet<Short>(Arrays.asList(refGroup)), data);
			datas.add(data);
			refs.addAll(Arrays.asList(refGroup));
		}
		
		actualCount = 0;
		key = iter.getHigherKey(null);
		while(key != null) {
			actualCount ++;
			datas.remove(iter.getValue(key));
			refs.remove(key);
			key = iter.getHigherKey(key);
		}
		assertEquals("Iterator didn't iterate over all datas!", expectedCount, actualCount);
		assertEquals("All refs should have been removed.", 0, refs.size());
		assertEquals("All datas should have been removed.", 0, datas.size());

		
		
		actualCount = 0;
		key = iter.getHigherKey(null);
		while(key != null) {
			actualCount ++;
			iter.removeValue(key);
			key = iter.getHigherKey(key);
		}
		assertEquals("Iterator didn't remove all datas.", refGroups.length, actualCount);
		assertEquals("DataMap should be empty.", 0, dataMap.size());
	}
}
