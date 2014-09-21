package net.ddns.mucaho.jnetrobust;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Delegate;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;
import net.ddns.mucaho.jnetrobust.util.TestHost;
import net.ddns.mucaho.jnetrobust.util.TestHost.TestHostListener;
import net.ddns.mucaho.jnetrobust.util.UnreliableQueue;
import net.ddns.mucaho.jnetrobust.util.UnreliableQueue.QueueListener;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class DelayedTest {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    @Injectable
    private QueueListener<Packet> queueListenerAtoB;
    @Injectable
    private QueueListener<Packet> queueListenerBtoA;
    @Injectable
    private ProtocolListener protocolListenerA;
    @Injectable
    private ProtocolListener protocolListenerB;
    @Injectable
    private TestHostListener<Long> hostListenerA;
    @Injectable
    private TestHostListener<Long> hostListenerB;

    public Object[][] parametersForTestDelayed() {
        Object[][] out = {{
                150, 200, 0.10f, 0.10f, 16, 20, true
        }};

        return out;
    }

    @SuppressWarnings({"unchecked"})
    @Test
    @Parameters
    public final void testDelayed(int minDelay, int maxDelay, float lossChance, float dupChance,
                                  int executeInterval, long executeTime, boolean retransmit)
            throws InterruptedException {

		/*
		 * Record phase
		 */
        final UnreliableQueue<Packet> aToB = new UnreliableQueue<Packet>(queueListenerAtoB,
                minDelay, maxDelay, lossChance, dupChance);
        final UnreliableQueue<Packet> bToA = new UnreliableQueue<Packet>(queueListenerBtoA,
                minDelay, maxDelay, lossChance, dupChance);


        final TestHost<Long> hostA = new TestHost<Long>(hostListenerA, new LongDataGenerator(),
                bToA, aToB, retransmit, new ProtocolConfig(protocolListenerA));
        final List<Long> sentA = new ArrayList<Long>();
        final List<Long> lostSentA = new ArrayList<Long>();
        final List<Long> dupedSentA = new ArrayList<Long>();
        final List<Long> receivedA = new ArrayList<Long>();
        final List<Long> ackedA = new ArrayList<Long>();
        final List<Long> notAckedA = new ArrayList<Long>();
        final List<Long> orderedA = new ArrayList<Long>();
        final List<Long> unorderedA = new ArrayList<Long>();
        final List<Long> retransmitsA = new ArrayList<Long>();
        final IntWrapper emptySendA = new IntWrapper();


        final TestHost<Long> hostB = new TestHost<Long>(hostListenerB, new LongDataGenerator(),
                aToB, bToA, retransmit, new ProtocolConfig(protocolListenerB));
        final List<Long> sentB = new ArrayList<Long>();
        final List<Long> lostSentB = new ArrayList<Long>();
        final List<Long> dupedSentB = new ArrayList<Long>();
        final List<Long> receivedB = new ArrayList<Long>();
        final List<Long> ackedB = new ArrayList<Long>();
        final List<Long> notAckedB = new ArrayList<Long>();
        final List<Long> orderedB = new ArrayList<Long>();
        final List<Long> unorderedB = new ArrayList<Long>();
        final List<Long> retransmitsB = new ArrayList<Long>();
        final IntWrapper emptySendB = new IntWrapper();


        new NonStrictExpectations() {{
            hostListenerA.notifyReceived(withCapture(receivedA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long received) {
                    System.out.println("[A-received]: " + received);
                }
            };
            hostListenerA.notifySent(withCapture(sentA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long sent) {
                    System.out.println("[A-sent]: " + sent);
                }
            };
            protocolListenerA.handleAckedTransmission(withCapture(ackedA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long acked) {
                    System.out.println("[A-acked]: " + acked);
                }
            };
            protocolListenerA.handleNotAckedTransmission(withCapture(notAckedA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long notAcked) {
                    System.out.println("[A-notAcked]: " + notAcked);
                }
            };
            protocolListenerA.handleOrderedTransmission(withCapture(orderedA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long ordered) {
                    System.out.println("[A-ordered]: " + ordered);
                }
            };
            protocolListenerA.handleUnorderedTransmission(withCapture(unorderedA));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long unordered) {
                    System.out.println("[A-unordered]: " + unordered);
                }
            };
            protocolListenerA.handleTransmissionRequest();
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate() {
                    emptySendA.value++;
                    System.out.println("[A-retransmit]");
                }
            };
            protocolListenerA.handleTransmissionRequests((Collection<? extends MultiKeyValue>) any);
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Collection<? extends MultiKeyValue> datas) {
                    for (MultiKeyValue data : datas)
                        retransmitsA.add((Long) data.getValue());
                    System.out.println("[A-retransmit]: " + Arrays.deepToString(datas.toArray()));
                }
            };
        }};

        new NonStrictExpectations() {{
            hostListenerB.notifyReceived(withCapture(receivedB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long received) {
                    System.out.println("[B-received]: " + received);
                }
            };
            hostListenerB.notifySent(withCapture(sentB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long sent) {
                    System.out.println("[B-sent]: " + sent);
                }
            };
            protocolListenerB.handleAckedTransmission(withCapture(ackedB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long acked) {
                    System.out.println("[B-acked]: " + acked);
                }
            };
            protocolListenerB.handleNotAckedTransmission(withCapture(notAckedB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long notAcked) {
                    System.out.println("[B-notAcked]: " + notAcked);
                }
            };
            protocolListenerB.handleOrderedTransmission(withCapture(orderedB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long ordered) {
                    System.out.println("[B-ordered]: " + ordered);
                }
            };
            protocolListenerB.handleUnorderedTransmission(withCapture(unorderedB));
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Long unordered) {
                    System.out.println("[B-unordered]: " + unordered);
                }
            };
            protocolListenerB.handleTransmissionRequest();
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate() {
                    emptySendB.value++;
                    System.out.println("[B-retransmit]");
                }
            };
            protocolListenerB.handleTransmissionRequests((Collection<? extends MultiKeyValue>) any);
            result = new Delegate<Void>() {
                @SuppressWarnings("unused")
                void delegate(Collection<? extends MultiKeyValue> datas) {
                    for (MultiKeyValue data : datas)
                        retransmitsB.add((Long) data.getValue());
                    System.out.println("[B-retransmit]: " + Arrays.deepToString(datas.toArray()));
                }
            };
        }};

        new NonStrictExpectations() {{
            queueListenerAtoB.notifyDuplicate((Packet) any);
            result = new Delegate<Packet>() {
                @SuppressWarnings("unused")
                void delegate(Packet dup) {
                    Long value = (Long) dup.getData().getValue();
                    dupedSentA.add(value);
                    System.out.println("[A-dupedSent]: " + value);
                }
            };
            queueListenerAtoB.notifyLoss((Packet) any);
            result = new Delegate<Packet>() {
                @SuppressWarnings("unused")
                void delegate(Packet loss) {
                    Long value = (Long) loss.getData().getValue();
                    lostSentA.add(value);
                    System.out.println("[A-lostSent]: " + value);
                }
            };

            queueListenerBtoA.notifyDuplicate((Packet) any);
            result = new Delegate<Packet>() {
                @SuppressWarnings("unused")
                void delegate(Packet dup) {
                    Long value = (Long) dup.getData().getValue();
                    dupedSentB.add(value);
                    System.out.println("[B-dupedSent]: " + value);
                }
            };
            queueListenerBtoA.notifyLoss((Packet) any);
            result = new Delegate<Packet>() {
                @SuppressWarnings("unused")
                void delegate(Packet loss) {
                    Long value = (Long) loss.getData().getValue();
                    lostSentB.add(value);
                    System.out.println("[B-lostSent]: " + value);
                }
            };
        }};
		
		/*
		 * Replay phase
		 */

        // play it for a longer interval
        executor.scheduleAtFixedRate(hostA, 0, executeInterval, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(hostB, executeInterval / 2, executeInterval, TimeUnit.MILLISECONDS);
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
        if (retransmit) {
            hostA.retransmit();
            hostB.retransmit();
        }
        hostA.send();
        hostB.send();
        Thread.sleep(maxDelay * 2); // wait for queue to make all elements available
        hostA.receive();
        hostB.receive();

        System.out.println();
		
		/*
		 * Verify phase
		 */

        if (retransmit) {
            new Verifications() {{
                protocolListenerA.handleNotAckedTransmission(any);
                times = 0;
                protocolListenerA.handleUnorderedTransmission(any);
                times = 0;
            }};
            new Verifications() {{
                protocolListenerB.handleNotAckedTransmission(any);
                times = 0;
                protocolListenerB.handleUnorderedTransmission(any);
                times = 0;
            }};
        }

        Long lastItem = null;
        for (Long item : orderedA) {
            if (lastItem != null) {
                assertTrue("ordered data should be ordered", item > lastItem);
            }
            lastItem = item;
        }
        lastItem = null;
        for (Long item : orderedB) {
            if (lastItem != null) {
                assertTrue("ordered data should be ordered", item > lastItem);
            }
            lastItem = item;
        }

        lastItem = null;
        for (Long item : unorderedA) {
            if (lastItem != null) {
                assertTrue("unordered data should be ordered", item > lastItem);
            }
            lastItem = item;

            assertTrue("unordered data should not have been orderly received", !orderedA.contains(item));
//			Long pred = item;
//			do {
//				pred--;
//			} while(unorderedA.contains(pred));
//			Long succ = item;
//			do {
//				succ++;
//			} while(unorderedA.contains(succ));
//			assertTrue("ordered data contains predecessor of unorderedData", orderedA.contains(pred));
//			assertTrue("ordered data contains successor of unorderedData", orderedA.contains(succ));
        }

        lastItem = null;
        for (Long item : unorderedB) {
            if (lastItem != null) {
                assertTrue("unordered data should be ordered", item > lastItem);
            }
            lastItem = item;

            assertTrue("unordered data should not have been orderly received", !orderedB.contains(item));
//			Long pred = item;
//			do {
//				pred--;
//			} while(unorderedB.contains(pred));
//			Long succ = item;
//			do {
//				succ++;
//			} while(unorderedB.contains(succ));
//			assertTrue("ordered data contains predecessor of unorderedData", orderedB.contains(pred));
//			assertTrue("ordered data contains successor of unorderedData", orderedB.contains(succ));
        }

//		assertEquals("no empty packets sent", 0, emptySendA.value);
        assertEquals("all packets acked", 0, notAckedA.size());
        assertEquals("all packets ordered", 0, unorderedA.size());
//		assertEquals("no empty packets sent", 0, emptySendB.value);
        assertEquals("all packets acked", 0, notAckedB.size());
        assertEquals("all packets ordered", 0, unorderedB.size());

        if (lossChance == 0f) {
            assertEquals("no lost packets", 0, lostSentB.size());
            assertEquals("no lost packets", 0, lostSentA.size());
        }

        if (dupChance == 0f) {
            assertEquals("no duped packets", 0, dupedSentB.size());
            assertEquals("no duped packets", 0, dupedSentA.size());
        }

        // the following addition of "magic constants" is due to the scheduling procedure of the very last messages
        assertEquals("all messages from A must be received at B", sentA.size() - lostSentA.size() + dupedSentA.size(), receivedB.size());
        assertEquals("all messages from A must be acked", sentA.size() - retransmitsA.size() - notAckedA.size() - 2, ackedA.size());
        assertEquals("all messages from A must be ordered at B", sentA.size() - retransmitsA.size() - unorderedB.size(), orderedB.size());

        // the following addition of "magic constants" is due to the scheduling procedure of the very last messages
        assertEquals("all messages from B must be received at A", sentB.size() - lostSentB.size() + dupedSentB.size() - 1, receivedA.size());
        assertEquals("all messages from B must be acked", sentB.size() - retransmitsB.size() - notAckedB.size() - 3, ackedB.size());
        assertEquals("all messages from B must be ordered at A", sentB.size() - retransmitsB.size() - unorderedA.size() - 1, orderedA.size());

    }


    private class IntWrapper {
        public int value = 0;
    }

    private class LongDataGenerator implements TestHost.TestHostDataGenerator<Long> {
        private long counter = -1; //Long.MIN_VALUE;

        @Override
        public Long generateData() {
            return ++counter;
        }
    }
}
