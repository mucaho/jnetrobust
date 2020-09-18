/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.EntryIterator;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.github.mucaho.jarrayliterals.ArrayShortcuts.*;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class SentSegmentMapOpTest {
    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    private final static AbstractSegmentMap dataMap = new SentSegmentMap();

    public enum Op {
        PUT_DATA, PUT_REF,
        PUTALL_DATA, PUTALL_REFS,
        REMOVE,
        REMOVEALL_DATA, REMOVEALL_REF, REMOVEALL_REFS,
        REPLACE;
    }

    public Object[][] parametersForTestMapOperations() {
        Object[][] out = (Object[][])
                $($(
                        (short)10, $S, Op.PUTALL_REFS, $S(1, 2, 3), $($S(1, 2, 3))
                ), $(
                        null, $S(1, 2, 3), Op.PUT_DATA, $S, $($S(1, 2, 3))
                ), $(
                        null, $S(1, 2, 3), Op.REMOVEALL_REF, $S(1), $null
                ), $(
                        (short)20, $S(4, 5), Op.PUTALL_DATA, $S, $($S(4, 5))
                ), $(
                        null, $S(4, 5), Op.PUT_REF, $S(6), $($S(4, 5, 6))
                ), $(
                        null, $S(4, 5, 6), Op.REMOVE, $S(5), $($S(4, 6))
                ), $(
                        (short)30, $S(7), Op.PUTALL_DATA, $S, $($S(4, 6), $S(7))
                ), $(
                        (short)40, $S, Op.PUTALL_REFS, $S(1, 2, 3), $($S(1, 2, 3), $S(4, 6), $S(7))
                ), $(
                        (short)50, $S, Op.REPLACE, $S(2), $($S(1, 3), $S(4, 6), $S(7), $S(2))
                ), $(
                        null, $S(1, 3), Op.REMOVEALL_DATA, $S, $($S(4, 6), $S(7), $S(2))
                ), $(
                        null, $S(7), Op.REMOVE, $S(7), $($S(4, 6), $S(2))
                ), $(
                        null, $S(6), Op.REMOVEALL_REFS, $S(4, 6), $($S(2))
                ), $(
                        null, $S(2), Op.REMOVE, $S(2), $null
                ));

        return out;
    }

    @Test
    @Parameters
    public final void testMapOperations(Short dataId, final Short[] initialRefs,
                                        final Op op, final Short[] opRefs, Short[][] expectedDataMap) {

        testDataOperations(dataId, initialRefs, op, opRefs);
        testMapState(expectedDataMap);
    }

    private void testDataOperations(Short dataId, final Short[] initialRefs,
                                    final Op op, final Short[] opRefs) {

        final Segment segment;
        if (dataId != null) {
            segment = new Segment(dataId, null);
        } else {
            segment = dataMap.getValue(initialRefs[0]);
            assertNotNull("There should be a valid segment.", segment);
        }

        final List<Short> addRefs = new ArrayList<Short>();
        final List<Short> removeRefs = new ArrayList<Short>();
        new NonStrictExpectations(segment) {{
            onInstance(segment).getTransmissionIds();
            result = new Delegate<Object>() {
                NavigableSet<Short> delegate() {
                    NavigableSet<Short> out = new TreeSet<Short>();
                    out.addAll(Arrays.asList(initialRefs));
                    out.addAll(addRefs);
                    out.removeAll(removeRefs);
                    return out;
                }
            };
            onInstance(segment).addTransmissionId(withCapture(addRefs));
            result = true;
            onInstance(segment).removeTransmissionId(withCapture(removeRefs));
            result = true;
        }};

        switch (op) {
            case PUT_DATA:
                // temporarily mock getLastTransmissionId, as it needs to return non-empty transmissionIds for this test to work
                new NonStrictExpectations() {{
                    onInstance(segment).getLastTransmissionId();
                    result = new Delegate<Object>() {
                        Short delegate() {
                            NavigableSet<Short> out = new TreeSet<Short>();
                            out.addAll(Arrays.asList(initialRefs));
                            out.addAll(addRefs);
                            out.removeAll(removeRefs);
                            return out.isEmpty() ? null : out.last();
                        }
                    };
                }};

                dataMap.put(segment);

                // remove temporary mock
                new NonStrictExpectations() {{
                    onInstance(segment).getLastTransmissionId();
                    result = new Delegate<Object>() {
                        Short delegate(Invocation invocation) {
                            return invocation.proceed();
                        }
                    };
                }};
                break;
            case PUT_REF:
                dataMap.put(opRefs[0], segment);
                break;
            case PUTALL_DATA:
                dataMap.putAll(segment);
                break;
            case PUTALL_REFS:
                dataMap.putAll(new TreeSet<Short>(Arrays.asList(opRefs)), segment);
                break;
            case REMOVE:
                dataMap.remove(opRefs[0]);
                break;
            case REMOVEALL_REF:
                dataMap.removeAll(opRefs[0]);
                break;
            case REMOVEALL_DATA:
                properlyMockOldSegmentTransmissionIds(segment, removeRefs, Collections.<Short>emptyList());
                dataMap.removeAll(segment);
                break;
            case REMOVEALL_REFS:
                properlyMockOldSegmentTransmissionIds(segment, removeRefs, Collections.<Short>emptyList());
                dataMap.removeAll(new TreeSet<Short>(Arrays.asList(opRefs)));
                break;
            case REPLACE:
                properlyMockOldSegmentTransmissionIds(dataMap.getValue(opRefs[0]), removeRefs, Arrays.asList(opRefs));
                dataMap.put(opRefs[0], segment);
                break;
        }

        new Verifications() {{
            if (op.toString().startsWith(Op.PUT_DATA.toString())) {
                // TODO: figure out what's wrong in the verification here
                // onInstance(segment).addTransmissionId(withEqual(initialRefs[initialRefs.length - 1])); times = 1;
                onInstance(segment).removeTransmissionId(anyShort); times = 0;
            } else if (op.toString().startsWith("PUT")) {
                for (Short addRef : addRefs) {
                    onInstance(segment).addTransmissionId(withEqual(addRef)); times = 1;
                }
                onInstance(segment).removeTransmissionId(anyShort); times = 0;
            } else if (op.toString().startsWith(Op.REMOVE.toString())) {
                for (Short removeRef : removeRefs) {
                    onInstance(segment).removeTransmissionId(withEqual(removeRef)); times = 1;
                }
                onInstance(segment).addTransmissionId(anyShort); times = 0;
            } else if (op == Op.REPLACE) {
                for (Short addRef : addRefs) {
                    onInstance(segment).addTransmissionId(withEqual(addRef)); times = 1;
                }
                onInstance(segment).removeTransmissionId(anyShort); times = 0;
            }
        }};
    }


    private void testMapState(Short[][] expectedDataMap) {
        if (expectedDataMap == null) {
            assertEquals("Data map should be null!", 0, dataMap.keySize());
            assertEquals("Data map should be null!", 0, dataMap.valueSize());
        } else {
            int elementCount = 0;

            Segment segment;
            for (Short[] dataRefs : expectedDataMap) {
                elementCount += dataRefs.length;

                segment = dataMap.getValue(dataRefs[0]);
                assertNotNull("Shouldn't be null!", segment);
                for (Short dataRef : dataRefs) {
                    assertSame("Should be same object!", segment, dataMap.getValue(dataRef));
                }
                assertArrayEquals("Should be equal!", dataRefs, dataMap.getKeys(segment).toArray());
            }

            assertEquals("Data map segment count mismatch.", elementCount, dataMap.keySize());
            assertEquals("Data map segment count mismatch.", expectedDataMap.length, dataMap.valueSize());
        }
    }

    private void properlyMockOldSegmentTransmissionIds(final Segment segment,
                                                        final List<Short> removeRefs,
                                                        final List<Short> replaceRefs) {
        // due to mocking transmissionIds, old already added instances have no transmissionIds
        // workaround this by manually re-adding the transmissionIds

        final NavigableSet<Short> toBeRemovedSegmentTransmissionIds = new TreeSet<Short>();

        EntryIterator<Short, Segment> iter = dataMap.getIterator();
        Short key = iter.getHigherKey(null);
        while (key != null) {
            if (iter.getValue(key) == segment && !replaceRefs.contains(key)) {
                toBeRemovedSegmentTransmissionIds.add(key);
            }
            key = iter.getHigherKey(key);
        }

        new NonStrictExpectations() {{
            onInstance(segment).getTransmissionIds();
            result = new Delegate<Object>() {
                NavigableSet<Short> delegate() {
                    toBeRemovedSegmentTransmissionIds.removeAll(removeRefs);
                    return toBeRemovedSegmentTransmissionIds;
                }
            };
        }};
    }
}
