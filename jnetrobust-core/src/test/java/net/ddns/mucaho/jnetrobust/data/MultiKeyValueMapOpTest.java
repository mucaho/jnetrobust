package net.ddns.mucaho.jnetrobust.data;

import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$S;
import static net.ddns.mucaho.jarrayliterals.ArrayShortcuts.$null;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.FullVerificationsInOrder;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValueMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import net.ddns.mucaho.jnetrobust.util.SequenceComparator;

@RunWith(JUnitParamsRunner.class)
public class MultiKeyValueMapOpTest {
	static {
		System.setProperty("jmockit-mockParameters", "annotated");
	}

	@Mocked
	private MultiKeyValue multiRefData;
	private final static MultiKeyValueMap dataMap = new MultiKeyValueMap(SequenceComparator.instance);
	
	public enum Op {
		PUT, 
		PUTALL_DATA, PUTALL_REFS, 
		REMOVE, 
		REMOVEALL_DATA, REMOVEALL_REF, REMOVEALL_REFS,
		REPLACE;
	}
	public Object[][] parametersForTestMapOperations() {
		Object[][] out = (Object[][]) 
		$($( 
			true, $S, Op.PUTALL_REFS, $S(1,2,3), 		$($S(1,2,3))
		),$(
			false, $S(1,2,3), Op.REMOVEALL_REF, $S(1),	$null
		),$( 
			true, $S(4,5), Op.PUTALL_DATA, $S,			$($S(4,5))
		),$(
			false, $S(4,5), Op.PUT, $S(6),				$($S(4,5,6))
		),$(
			false, $S(4,5,6), Op.REMOVE, $S(5),			$($S(4,6))
		),$(
			true, $S(7), Op.PUTALL_DATA, $S,			$($S(4,6), $S(7))
		),$(
			true, $S, Op.PUTALL_REFS, $S(1,2,3),		$($S(1,2,3), $S(4,6), $S(7))
		),$(
			true, $S, Op.REPLACE, $S(2), 				$($S(1,3), $S(4,6), $S(7), $S(2))
		),$(
			false, $S(1,3), Op.REMOVEALL_DATA, $S, 		$($S(4,6), $S(7), $S(2))
		),$(
			false, $S(7), Op.REMOVE, $S(7),				$($S(4,6), $S(2))
		),$(
			false, $S(6), Op.REMOVEALL_REFS, $S(4,6), 	$($S(2))
		),$(
			false, $S(2), Op.REMOVE, $S(2), 			$null
		));
		
		return out;
	}
	
	@Test
	@Parameters
	public final void testMapOperations(Boolean createNew, final Short[] preRefs, 
			final Op op, final Short[] opRefs, Short[][] expectedDataMap) {
		
		testDataOperations(createNew, preRefs, op, opRefs);
		testMapState(expectedDataMap);
	}

	private void testDataOperations(Boolean createNew, final Short[] initialRefs, 
			final Op op, final Short[] opRefs) {
		
		final MultiKeyValue data;
		if (createNew) {
			data = new MultiKeyValue();
		} else {
			data = dataMap.get(initialRefs[0]);
			assertNotNull("There should be a valid data.", data);
		}
				
		final List<Short> addRefs = new ArrayList<Short>();
		final List<Short> removeRefs = new ArrayList<Short>();
		new NonStrictExpectations() {{
			onInstance(data).getDynamicReferences(); result = new TreeSet<Short>(Arrays.asList(initialRefs));
			onInstance(data).addDynamicReference(withCapture(addRefs)); result = true;
			onInstance(data).removeDynamicReference(withCapture(removeRefs)); result = true;
		}};

		switch(op) {
		case PUT:				dataMap.put(opRefs[0], data); break;
		case PUTALL_DATA:		dataMap.putAll(data); break;
		case PUTALL_REFS:		
			dataMap.putAll(new TreeSet<Short>(Arrays.asList(opRefs)), data); break;
		case REMOVE:			dataMap.remove(opRefs[0]); break;
		case REMOVEALL_REF:		dataMap.removeAll(opRefs[0]); break;
		case REMOVEALL_DATA:	dataMap.removeAll(data); break;
		case REMOVEALL_REFS:	
			dataMap.removeAll(new TreeSet<Short>(Arrays.asList(opRefs))); break;
		case REPLACE:			dataMap.put(opRefs[0], data); break;
		}
		
		new FullVerificationsInOrder() {{
			if (op == Op.PUTALL_DATA || op == Op.REMOVEALL_REF || op == Op.REMOVEALL_DATA)
				onInstance(data).getDynamicReferences();
			
			if (op.toString().startsWith(Op.PUT.toString())) {
				for (Short addRef: addRefs)
					onInstance(data).addDynamicReference(withEqual(addRef));
			} else if (op.toString().startsWith(Op.REMOVE.toString())){
				for (Short removeRef: removeRefs)
					onInstance(data).removeDynamicReference(withEqual(removeRef));
			} else if (op == Op.REPLACE) {
				for (Short addRef: addRefs)
					onInstance(data).addDynamicReference(withEqual(addRef));
				multiRefData.removeDynamicReference(opRefs[0]);
			}
		}};
	}
	

	private void testMapState(Short[][] expectedDataMap) {
		if (expectedDataMap == null) {
			assertEquals("Data map should be null!", 0, dataMap.size());
		} else {
			int elementCount = 0;
			
			MultiKeyValue element;
			for (Short[] dataRefs: expectedDataMap) {
				element = dataMap.get(dataRefs[0]);
				assertNotNull("Shouldn't be null!", element);
				for (Short dataRef: dataRefs) {
					assertSame("Should be same object!", element, dataMap.get(dataRef));
					elementCount++;
				}
			}
			
			assertEquals("Data map element count mismatch.", elementCount, dataMap.size());
		}
	}
}
