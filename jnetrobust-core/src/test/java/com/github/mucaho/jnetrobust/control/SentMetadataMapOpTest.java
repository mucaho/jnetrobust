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
public class SentMetadataMapOpTest {
    static {
        System.setProperty("jmockit-mockParameters", "annotated");
    }

    private final static AbstractMetadataMap<Object> dataMap = new SentMetadataMap<Object>();

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

        final Metadata<Object> metadata;
        if (dataId != null) {
            metadata = new Metadata<Object>(dataId, null);
        } else {
            metadata = dataMap.getValue(initialRefs[0]);
            assertNotNull("There should be a valid metadata.", metadata);
        }

        final List<Short> addRefs = new ArrayList<Short>();
        final List<Short> removeRefs = new ArrayList<Short>();
        new NonStrictExpectations(metadata) {{
            onInstance(metadata).getTransmissionIds();
            result = new Delegate<Object>() {
                NavigableSet<Short> delegate() {
                    NavigableSet<Short> out = new TreeSet<Short>();
                    out.addAll(Arrays.asList(initialRefs));
                    out.addAll(addRefs);
                    out.removeAll(removeRefs);
                    return out;
                }
            };
            onInstance(metadata).addTransmissionId(withCapture(addRefs));
            result = true;
            onInstance(metadata).removeTransmissionId(withCapture(removeRefs));
            result = true;
        }};

        switch (op) {
            case PUT_DATA:
                // temporarily mock getLastTransmissionId, as it needs to return non-empty transmissionIds for this test to work
                new NonStrictExpectations() {{
                    onInstance(metadata).getLastTransmissionId();
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

                dataMap.put(metadata);

                // remove temporary mock
                new NonStrictExpectations() {{
                    onInstance(metadata).getLastTransmissionId();
                    result = new Delegate<Object>() {
                        Short delegate(Invocation invocation) {
                            return invocation.proceed();
                        }
                    };
                }};
                break;
            case PUT_REF:
                dataMap.put(opRefs[0], metadata);
                break;
            case PUTALL_DATA:
                dataMap.putAll(metadata);
                break;
            case PUTALL_REFS:
                dataMap.putAll(new TreeSet<Short>(Arrays.asList(opRefs)), metadata);
                break;
            case REMOVE:
                dataMap.remove(opRefs[0]);
                break;
            case REMOVEALL_REF:
                dataMap.removeAll(opRefs[0]);
                break;
            case REMOVEALL_DATA:
                properlyMockOldMetadataTransmissionIds(metadata, removeRefs, Collections.<Short>emptyList());
                dataMap.removeAll(metadata);
                break;
            case REMOVEALL_REFS:
                properlyMockOldMetadataTransmissionIds(metadata, removeRefs, Collections.<Short>emptyList());
                dataMap.removeAll(new TreeSet<Short>(Arrays.asList(opRefs)));
                break;
            case REPLACE:
                properlyMockOldMetadataTransmissionIds(dataMap.getValue(opRefs[0]), removeRefs, Arrays.asList(opRefs));
                dataMap.put(opRefs[0], metadata);
                break;
        }

        new Verifications() {{
            if (op.toString().startsWith(Op.PUT_DATA.toString())) {
                // TODO: figure out what's wrong in the verification here
                // onInstance(metadata).addTransmissionId(withEqual(initialRefs[initialRefs.length - 1])); times = 1;
                onInstance(metadata).removeTransmissionId(anyShort); times = 0;
            } else if (op.toString().startsWith("PUT")) {
                for (Short addRef : addRefs) {
                    onInstance(metadata).addTransmissionId(withEqual(addRef)); times = 1;
                }
                onInstance(metadata).removeTransmissionId(anyShort); times = 0;
            } else if (op.toString().startsWith(Op.REMOVE.toString())) {
                for (Short removeRef : removeRefs) {
                    onInstance(metadata).removeTransmissionId(withEqual(removeRef)); times = 1;
                }
                onInstance(metadata).addTransmissionId(anyShort); times = 0;
            } else if (op == Op.REPLACE) {
                for (Short addRef : addRefs) {
                    onInstance(metadata).addTransmissionId(withEqual(addRef)); times = 1;
                }
                onInstance(metadata).removeTransmissionId(anyShort); times = 0;
            }
        }};
    }


    private void testMapState(Short[][] expectedDataMap) {
        if (expectedDataMap == null) {
            assertEquals("Data map should be null!", 0, dataMap.keySize());
            assertEquals("Data map should be null!", 0, dataMap.valueSize());
        } else {
            int elementCount = 0;

            Metadata<Object> metadata;
            for (Short[] dataRefs : expectedDataMap) {
                elementCount += dataRefs.length;

                metadata = dataMap.getValue(dataRefs[0]);
                assertNotNull("Shouldn't be null!", metadata);
                for (Short dataRef : dataRefs) {
                    assertSame("Should be same object!", metadata, dataMap.getValue(dataRef));
                }
                assertArrayEquals("Should be equal!", dataRefs, dataMap.getKeys(metadata).toArray());
            }

            assertEquals("Data map metadata count mismatch.", elementCount, dataMap.keySize());
            assertEquals("Data map metadata count mismatch.", expectedDataMap.length, dataMap.valueSize());
        }
    }

    private void properlyMockOldMetadataTransmissionIds(final Metadata<Object> metadata,
                                                        final List<Short> removeRefs,
                                                        final List<Short> replaceRefs) {
        // due to mocking transmissionIds, old already added instances have no transmissionIds
        // workaround this by manually re-adding the transmissionIds

        final NavigableSet<Short> toBeRemovedMetadataTransmissionIds = new TreeSet<Short>();

        EntryIterator<Short, Metadata<Object>> iter = dataMap.getIterator();
        Short key = iter.getHigherKey(null);
        while (key != null) {
            if (iter.getValue(key) == metadata && !replaceRefs.contains(key)) {
                toBeRemovedMetadataTransmissionIds.add(key);
            }
            key = iter.getHigherKey(key);
        }

        new NonStrictExpectations() {{
            onInstance(metadata).getTransmissionIds();
            result = new Delegate<Object>() {
                NavigableSet<Short> delegate() {
                    toBeRemovedMetadataTransmissionIds.removeAll(removeRefs);
                    return toBeRemovedMetadataTransmissionIds;
                }
            };
        }};
    }
}
