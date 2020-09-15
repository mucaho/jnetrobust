/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.AbstractMapControl;
import com.github.mucaho.jnetrobust.control.AbstractMetadataMap;
import com.github.mucaho.jnetrobust.controller.Controller;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.*;
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.util.DebugProtocolListener;
import com.github.mucaho.jnetrobust.util.TestHost;
import com.github.mucaho.jnetrobust.util.TestHost.TestHostListener;
import com.github.mucaho.jnetrobust.util.UnreliableQueue;
import com.github.mucaho.jnetrobust.util.UnreliableQueue.QueueListener;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class DelayedTest {
    private final static boolean DEBUG = false;

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    @Injectable
    private QueueListener<Packet<Long>> queueListenerAtoB;
    @Injectable
    private QueueListener<Packet<Long>> queueListenerBtoA;
    @Injectable
    private ProtocolListener<Long> protocolListenerA;
    @Injectable
    private ProtocolListener<Long> protocolListenerB;
    @Injectable
    private TestHostListener<Long> hostListenerA;
    @Injectable
    private TestHostListener<Long> hostListenerB;

    public Object[][] parametersForTestDelayed() {
        Object[][] out = {{
                150, 200, 0.10f, 0.05f, 16, 10, true
                // 40, 60000, 0.75f, 0.25f, 16, 1000, true
        }};

        return out;
    }

    @Test
    @Parameters
    @SuppressWarnings("unchecked")
    public final void testDelayed(int minDelay, int maxDelay, float lossChance, float dupChance,
                                  int executeInterval, long executeTime, boolean retransmit)
            throws InterruptedException {

		/*
		 * Record phase
		 */
        final UnreliableQueue<Packet<Long>> aToB = new UnreliableQueue<Packet<Long>>(queueListenerAtoB,
                minDelay, maxDelay, lossChance, dupChance);
        final UnreliableQueue<Packet<Long>> bToA = new UnreliableQueue<Packet<Long>>(queueListenerBtoA,
                minDelay, maxDelay, lossChance, dupChance);


        ProtocolListener<Long> listenerA = DEBUG ?
                new DebugProtocolListener<Long>(protocolListenerA, Logger.getConsoleLogger("A")) :
                protocolListenerA;
        final TestHost<Long> hostA = new TestHost<Long>(hostListenerA, new LongDataGenerator(),
                bToA, aToB, listenerA, new ProtocolConfig(), DEBUG ? "A" : null);
        final List<Long> sentA = new ArrayList<Long>();
        final List<Long> lostSentA = new ArrayList<Long>();
        final List<Long> dupedSentA = new ArrayList<Long>();
        final List<Long> receivedA = new ArrayList<Long>();
        final List<Long> ackedA = new ArrayList<Long>();
        final List<Long> notAckedA = new ArrayList<Long>();
        final List<Long> orderedA = new ArrayList<Long>();
        final List<Long> unorderedA = new ArrayList<Long>();
        final List<Long> retransmitsA = new ArrayList<Long>();


        ProtocolListener<Long> listenerB = DEBUG ?
                new DebugProtocolListener<Long>(protocolListenerB, Logger.getConsoleLogger("B")) :
                protocolListenerB;
        final TestHost<Long> hostB = new TestHost<Long>(hostListenerB, new LongDataGenerator(),
                aToB, bToA, listenerB, new ProtocolConfig(), DEBUG ? "B" : null);
        final List<Long> sentB = new ArrayList<Long>();
        final List<Long> lostSentB = new ArrayList<Long>();
        final List<Long> dupedSentB = new ArrayList<Long>();
        final List<Long> receivedB = new ArrayList<Long>();
        final List<Long> ackedB = new ArrayList<Long>();
        final List<Long> notAckedB = new ArrayList<Long>();
        final List<Long> orderedB = new ArrayList<Long>();
        final List<Long> unorderedB = new ArrayList<Long>();
        final List<Long> retransmitsB = new ArrayList<Long>();


        new NonStrictExpectations() {{
            hostListenerA.notifyReceived(with(new Delegate<Long>() {
                @SuppressWarnings("unused")
                void delegate(Long data) {
                    receivedA.add(data);
                    if (DEBUG)
                        System.out.println("[A-received]: " + data);
                }
            }));
            hostListenerA.notifySent(with(new Delegate<Long>() {
                @SuppressWarnings("unused")
                void delegate(Long data) {
                    sentA.add(data);
                    if (DEBUG)
                        System.out.println("[A-sent]: " + data);
                }
            }));

            protocolListenerA.handleAckedData(anyShort, withCapture(ackedA));
            protocolListenerA.handleUnackedData(anyShort, withCapture(notAckedA));
            protocolListenerA.handleOrderedData(anyShort, withCapture(orderedA));
            protocolListenerA.handleUnorderedData(anyShort, withCapture(unorderedA));
            protocolListenerA.shouldRetransmit(anyShort, withCapture(retransmitsA)); result = null;
        }};

        new NonStrictExpectations() {{
            hostListenerB.notifyReceived(with(new Delegate<Long>() {
                @SuppressWarnings("unused")
                void delegate(Long data) {
                    receivedB.add(data);
                    if (DEBUG)
                        System.out.println("[B-received]: " + data);
                }
            }));
            hostListenerB.notifySent(with(new Delegate<Long>() {
                @SuppressWarnings("unused")
                void delegate(Long data) {
                    sentB.add(data);
                    if (DEBUG)
                        System.out.println("[B-sent]: " + data);
                }
            }));

            protocolListenerB.handleAckedData(anyShort, withCapture(ackedB));
            protocolListenerB.handleUnackedData(anyShort, withCapture(notAckedB));
            protocolListenerB.handleOrderedData(anyShort, withCapture(orderedB));
            protocolListenerB.handleUnorderedData(anyShort, withCapture(unorderedB));
            protocolListenerB.shouldRetransmit(anyShort, withCapture(retransmitsB)); result = null;
        }};

        new NonStrictExpectations() {{
            queueListenerAtoB.notifyDuplicate((Packet<Long>) any); result = new Delegate<Packet<Long>>() {
                @SuppressWarnings("unused")
                void delegate(Packet<Long> dup) {
                    for (Metadata<Long> metadata: dup.getMetadatas()) {
                        dupedSentA.add(metadata.getData());
                        if (DEBUG)
                            System.out.println("[A-dupedSent]: " + metadata.getData());
                    }
                }
            };
            queueListenerAtoB.notifyLoss((Packet<Long>) any); result = new Delegate<Packet<Long>>() {
                @SuppressWarnings("unused")
                void delegate(Packet<Long> loss) {
                    for (Metadata<Long> metadata: loss.getMetadatas()) {
                        lostSentA.add(metadata.getData());
                        if (DEBUG)
                            System.out.println("[A-lostSent]: " + metadata.getData());
                    }
                }
            };

            queueListenerBtoA.notifyDuplicate((Packet<Long>) any); result = new Delegate<Packet<Long>>() {
                @SuppressWarnings("unused")
                void delegate(Packet<Long> dup) {
                    for (Metadata<Long> metadata: dup.getMetadatas()) {
                        dupedSentB.add(metadata.getData());
                        if (DEBUG)
                            System.out.println("[B-dupedSent]: " + metadata.getData());
                    }
                }
            };
            queueListenerBtoA.notifyLoss((Packet<Long>) any); result = new Delegate<Packet<Long>>() {
                @SuppressWarnings("unused")
                void delegate(Packet<Long> loss) {
                    for (Metadata<Long> metadata: loss.getMetadatas()) {
                        lostSentB.add(metadata.getData());
                        if (DEBUG)
                            System.out.println("[B-lostSent]: " + metadata.getData());
                    }
                }
            };
        }};
		
		/*
		 * Replay phase
		 */

        // play it for a longer interval
        executor.scheduleAtFixedRate(hostA, 0, executeInterval, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(hostB, executeInterval/2, executeInterval, TimeUnit.MILLISECONDS);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                // enable reliable queue mode for final messages
                aToB.setDupChance(0f);
                aToB.setLossChance(0f);
                aToB.setMinDelay(0L);
                aToB.setMaxDelay(0L);
                bToA.setDupChance(0f);
                bToA.setLossChance(0f);
                bToA.setMinDelay(0L);
                bToA.setMaxDelay(0L);
            }
        }, executeTime - executeTime / 10, TimeUnit.SECONDS);
        executor.awaitTermination(executeTime, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(executeInterval * 2 + maxDelay * 2, TimeUnit.MILLISECONDS);

        // let pending messages finish
        Thread.sleep(maxDelay * 2);
        hostA.receive();
        hostB.receive();
        hostA.send();
        hostB.send();
        Thread.sleep(maxDelay * 2); // wait for queue to make all elements available
        hostA.receive();
        hostB.receive();

        System.out.println();
		
		/*
		 * Verify phase
		 */

        // determine occurence counts

        final Map<Long, Integer> sentAOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> ackedAOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> notAckedAOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> orderedAOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> unorderedAOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> retransmitsAOccurrences = new HashMap<Long, Integer>();

        final Map<Long, Integer> sentBOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> ackedBOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> notAckedBOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> orderedBOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> unorderedBOccurrences = new HashMap<Long, Integer>();
        final Map<Long, Integer> retransmitsBOccurrences = new HashMap<Long, Integer>();

        for (Long item : sentA) {
            Integer count = sentAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            sentAOccurrences.put(item, count);
        }
        for (Long item : ackedA) {
            Integer count = ackedAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            ackedAOccurrences.put(item, count);
        }
        for (Long item : notAckedA) {
            Integer count = notAckedAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            notAckedAOccurrences.put(item, count);;
        }
        for (Long item : orderedA) {
            Integer count = orderedAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            orderedAOccurrences.put(item, count);
        }
        for (Long item : unorderedA) {
            Integer count = unorderedAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            unorderedAOccurrences.put(item, count);
        }
        for (Long item : retransmitsA) {
            Integer count = retransmitsAOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            retransmitsAOccurrences.put(item, count);
        }

        for (Long item : sentB) {
            Integer count = sentBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            sentBOccurrences.put(item, count);
        }
        for (Long item : ackedB) {
            Integer count = ackedBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            ackedBOccurrences.put(item, count);
        }
        for (Long item : notAckedB) {
            Integer count = notAckedBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            notAckedBOccurrences.put(item, count);
        }
        for (Long item : orderedB) {
            Integer count = orderedBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            orderedBOccurrences.put(item, count);
        }
        for (Long item : unorderedB) {
            Integer count = unorderedBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            unorderedBOccurrences.put(item, count);
        }
        for (Long item : retransmitsB) {
            Integer count = retransmitsBOccurrences.get(item);
            count = count == null ? 1 : count + 1;
            retransmitsBOccurrences.put(item, count);
        }

        // do verifications

        for (Long item : notAckedA)
            assertTrue("notAcked data should not have been acked", !ackedAOccurrences.containsKey(item));
        for (Long item : notAckedB)
            assertTrue("notAcked data should not have been acked", !ackedBOccurrences.containsKey(item));

        for (Long item : retransmitsA)
            assertTrue("retransmitted data should have been sent from sender", sentAOccurrences.containsKey(item));
        for (Long item : retransmitsB)
            assertTrue("retransmitted data should have been sent from sender", sentBOccurrences.containsKey(item));
        for (Long item : lostSentA)
            assertTrue("over medium lost data should have been sent from sender", sentAOccurrences.containsKey(item));
        for (Long item : lostSentB)
            assertTrue("over medium lost data should have been sent from sender", sentBOccurrences.containsKey(item));
        for (Long item : dupedSentA)
            assertTrue("over medium duplicated data should have been sent from sender", sentAOccurrences.containsKey(item));
        for (Long item : dupedSentB)
            assertTrue("over medium duplicated data should have been sent from sender", sentBOccurrences.containsKey(item));

        Long lastItem = null;
        for (Long item : orderedA) {
            assertTrue("orderly received data should have been sent from sender", sentBOccurrences.containsKey(item));

            if (lastItem != null) {
                assertTrue("ordered data should be ordered", item > lastItem);
            }
            lastItem = item;
        }
        lastItem = null;
        for (Long item : orderedB) {
            assertTrue("orderly received data should have been sent from sender", sentAOccurrences.containsKey(item));

            if (lastItem != null) {
                assertTrue("ordered data should be ordered", item > lastItem);
            }
            lastItem = item;
        }

        lastItem = null;
        for (Long item : unorderedA) {
            assertTrue("unorderly received data should have been sent from sender", sentBOccurrences.containsKey(item));

            if (lastItem != null) {
                assertTrue("unordered data should be ordered", item > lastItem);
            }
            lastItem = item;

            assertTrue("unordered data should not have been orderly received", !orderedAOccurrences.containsKey(item));

// The following assertions can not be guaranteed, since there may be multiple unordered events and multiple holes until an ordered event occurs
//			Long pred = item;
//			do {
//				pred--;
//			} while(unorderedAOccurrences.containsKey(pred));
//			Long succ = item;
//			do {
//				succ++;
//			} while(unorderedAOccurrences.containsKey(succ));
//			assertTrue("ordered data contains predecessor of unorderedData", orderedAOccurrences.containsKey(pred));
//			assertTrue("ordered data contains successor of unorderedData", orderedAOccurrences.containsKey(succ));
        }

        lastItem = null;
        for (Long item : unorderedB) {
            assertTrue("orderly received data should have been sent from sender", sentAOccurrences.containsKey(item));

            if (lastItem != null) {
                assertTrue("unordered data should be ordered", item > lastItem);
            }
            lastItem = item;

            assertTrue("unordered data should not have been orderly received", !orderedBOccurrences.containsKey(item));

// The following assertions can not be guaranteed, since there may be multiple unordered events and multiple holes until an ordered event occurs
//			Long pred = item;
//			do {
//				pred--;
//			} while(unorderedBOccurrences.containsKey(pred));
//			Long succ = item;
//			do {
//				succ++;
//			} while(unorderedBOccurrences.containsKey(succ));
//			assertTrue("ordered data contains predecessor of unorderedData", orderedBOccurrences.containsKey(pred));
//			assertTrue("ordered data contains successor of unorderedData", orderedBOccurrences.containsKey(succ));
        }

        // the following addition of "magic constants" is due to the scheduling procedure of the very last messages
        assertEquals("all messages from A must be received at B",
                sentA.size() - lostSentA.size() + dupedSentA.size(),
                receivedB.size());
        assertEquals("all messages from A must be acked",
                sentA.size() - retransmitsA.size() - 1,
                ackedA.size()  + notAckedA.size());
        if (lossChance == 0f || (retransmit && lossChance <= 0.15f)) {
            assertEquals("all messages from A must be ordered at B, given a 'reasonable' package loss chance",
                    sentA.size() - retransmitsA.size(),
                    orderedB.size() + unorderedB.size());
        }

        // the following addition of "magic constants" is due to the scheduling procedure of the very last messages
        assertEquals("all messages from B must be received at A",
                sentB.size() - lostSentB.size() + dupedSentB.size(),
                receivedA.size());
        assertEquals("all messages from B must be acked",
                sentB.size() - retransmitsB.size() - 1,
                ackedB.size() + notAckedB.size());
        if (lossChance == 0f || (retransmit && lossChance <= 0.15f)) {
            assertEquals("all messages from B must be ordered at A, given a 'reasonable' package loss chance",
                    sentB.size() - retransmitsB.size(),
                    orderedA.size() + unorderedA.size());
        }

        if (lossChance == 0f) {
            assertEquals("no lost packets", 0, lostSentB.size());
            assertEquals("no lost packets", 0, lostSentA.size());
        }

        if (dupChance == 0f) {
            assertEquals("no duped packets", 0, dupedSentB.size());
            assertEquals("no duped packets", 0, dupedSentA.size());
        }

        // given a "reasonable" package loss chance
        if (lossChance == 0f || (retransmit && lossChance <= 0.15f)) {
            new Verifications() {{
                protocolListenerA.handleUnackedData(anyShort, anyLong); times = 0;
                protocolListenerA.handleUnorderedData(anyShort, anyLong); times = 0;
            }};
            new Verifications() {{
                protocolListenerB.handleUnackedData(anyShort, anyLong); times = 0;
                protocolListenerB.handleUnorderedData(anyShort, anyLong); times = 0;
            }};

            assertEquals("all packets acked", 0, notAckedA.size());
            assertEquals("all packets ordered", 0, unorderedA.size());
            assertEquals("all packets acked", 0, notAckedB.size());
            assertEquals("all packets ordered", 0, unorderedB.size());
        }

        // additionally make sure internal data structures are empty and properly exhaused
        // otherwise this is a good indicator something went wrong
        {
            DelayQueue aToBQueue = Deencapsulation.getField(aToB, "queue");
            assertEquals(0, aToBQueue.size());
            DelayQueue bToAQueue = Deencapsulation.getField(bToA, "queue");
            assertEquals(0, bToAQueue.size());

            Protocol protocolA = Deencapsulation.getField(hostA, "protocol");
            Controller controllerA = Deencapsulation.getField(protocolA, "controller");
            {
                AbstractMapControl sentMapControl = Deencapsulation.getField(controllerA, "sentMapControl");
                AbstractMetadataMap dataMap = Deencapsulation.getField(sentMapControl, "dataMap");
                Map keyMap = Deencapsulation.getField(dataMap, "keyMap");
                assertEquals(1, keyMap.size());
                Map valueMap = Deencapsulation.getField(dataMap, "valueMap");
                assertEquals(1, valueMap.size());
            }
            {
                AbstractMapControl receivedMapControl = Deencapsulation.getField(controllerA, "receivedMapControl");
                AbstractMetadataMap dataMap = Deencapsulation.getField(receivedMapControl, "dataMap");
                Map keyMap = Deencapsulation.getField(dataMap, "keyMap");
                assertEquals(0, keyMap.size());
                Map valueMap = Deencapsulation.getField(dataMap, "valueMap");
                assertEquals(0, valueMap.size());
            }

            Protocol protocolB = Deencapsulation.getField(hostB, "protocol");
            Controller controllerB = Deencapsulation.getField(protocolB, "controller");
            {
                AbstractMapControl sentMapControl = Deencapsulation.getField(controllerB, "sentMapControl");
                AbstractMetadataMap dataMap = Deencapsulation.getField(sentMapControl, "dataMap");
                Map keyMap = Deencapsulation.getField(dataMap, "keyMap");
                assertEquals(1, keyMap.size());
                Map valueMap = Deencapsulation.getField(dataMap, "valueMap");
                assertEquals(1, valueMap.size());
            }
            {
                AbstractMapControl receivedMapControl = Deencapsulation.getField(controllerB, "receivedMapControl");
                AbstractMetadataMap dataMap = Deencapsulation.getField(receivedMapControl, "dataMap");
                Map keyMap = Deencapsulation.getField(dataMap, "keyMap");
                assertEquals(0, keyMap.size());
                Map valueMap = Deencapsulation.getField(dataMap, "valueMap");
                assertEquals(0, valueMap.size());
            }
        }


    }

    private class LongDataGenerator implements TestHost.TestHostDataGenerator<Long> {
        private long counter = -1; //Long.MIN_VALUE;

        @Override
        public Long generateData() {
            return ++counter;
        }
    }
}
