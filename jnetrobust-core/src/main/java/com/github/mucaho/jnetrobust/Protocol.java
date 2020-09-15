/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.controller.Controller;
import com.github.mucaho.jnetrobust.controller.DebugController;
import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.util.CollectionUtils;
import com.github.mucaho.jnetrobust.util.DebugProtocolListener;
import com.github.mucaho.jnetrobust.util.IdComparator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * The central API class that represents the virtual protocol.
 * It maintains the state of a virtual protocol instance and
 * offers methods to alter that state by wrapping user data with protocol specific metadata. <p></p>
 * Various constructors are offered to instantiate a new protocol instance. <p></p>
 * Typically you call the protocol's {@link Protocol#send(Object)}
 * and {@link Protocol#receive(Packet)} methods in order to attach/detach protocol-specific information
 * to your user data. This protocol-specific information and the protocol's internal state is then used
 * to enable reliable & ordered communication even over an unreliable medium. <br></br>
 * Note that both {@link Protocol#send(Object)} and {@link Protocol#receive(Packet)} methods may trigger zero or more
 * {@link ProtocolListener listener} events before they return. <br></br>
 * How serialization is done and over which medium the {@link Packet packaged user-data} is sent is
 * up to the user (analogue for receiving and deserialization). <br></br>
 * Note that always the <b>same two protocol instances must communicate with each other</b>, as they share a distributed
 * state together. <br></br>
 * In order for the protocol to work the user <b>must immediately acknowledge received data</b>, either by sending
 * an empty transmission or sending new user-data if it is available. The user may delay doing this, if he is sending
 * new data at a <b>fixed interval &lt; 50ms</b>. <br></br>
 * The {@link Protocol#send(Object, java.io.ObjectOutput)} & {@link Protocol#receive(java.io.ObjectInput)}
 * are utility methods which automatically write the packaged user-data to a {@link java.io.ObjectOutput} or
 * read the packaged user-data from a {@link java.io.ObjectInput} respectively. Other than that, they behave
 * exactly like the {@link Protocol#send(Object)} and {@link Protocol#receive(Packet)} methods.<p></p>
 * This class also offer utility methods to query the {@link Protocol#getSmoothedRTT() round-trip time}, as well
 * as the {@link Protocol#getRTTVariation() round-trip time variation}. <p></p>
 * This class also offers the ability to {@link Protocol#compare(Short, Short) compare <code>dataIds</code>} against each other. The user <b>must not compare these ids</b> with built-in comparison
 * operators. These ids wrap around to their {@link Short#MIN_VALUE min value} once they are incremented beyond
 * their {@link Short#MAX_VALUE max value}, hence this compare method must be used.
 *
 * @param <T> the user data type
 */
public class Protocol<T> implements Comparator<Short> {
    private final Controller<T> controller;
    // TODO: possibly add currentlyInUse boolean to prevent reentering send / receive procedure while listeners fire

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener<T> listener) {
        this(listener, new ProtocolConfig(), null);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolConfig config) {
        this(new ProtocolListener<T>(), config, null);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(Logger logger) {
        this(new ProtocolListener<T>(), new ProtocolConfig(), logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolConfig config, Logger logger) {
        this(new ProtocolListener<T>(), config, logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener<T> listener, Logger logger) {
        this(listener, new ProtocolConfig(), logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener<T> listener, ProtocolConfig config) {
        this(listener, config, null);
    }

    /**
     * Construct a new protocol instance using the supplied {@link ProtocolListener protocol listener}
     * and {@link ProtocolConfig protocol configuration}.
     * Internal state changes will be logged using the supplied {@link Logger logger}.
     * @param listener  the <code>protocol listener</code> which will be informed about various protocol events
     * @param config    the <code>protocol configuration</code> which will be used to configure this protocol instance
     * @param logger    the <code>Logger</code> which will be used to log internal state changes
     */
    public Protocol(ProtocolListener<T> listener, ProtocolConfig config, Logger logger) {
        if (listener == null) listener = new ProtocolListener<T>();
        if (config == null) config = new ProtocolConfig();

        if (logger != null) {
            ProtocolListener<T> debugListener = new DebugProtocolListener<T>(listener, logger);
            this.controller = new DebugController<T>(debugListener, config, logger);
        } else {
            this.controller = new Controller<T>(listener, config);
        }
    }

    private final List<T> sendDatas = new ArrayList<T>();
    private final NavigableMap<Short, Packet<T>> sentPacketMap = new TreeMap<Short, Packet<T>>(IdComparator.instance);
    private final NavigableMap<Short, Packet<T>> sentPacketMapOut = CollectionUtils.unmodifiableNavigableMap(sentPacketMap);
    private final PacketEntry<T> sentPacketOut = new PacketEntry<T>();

    /**
     * Convenience method does the same as {@link Protocol#send(Object) <code>send(null)</code>}.
     * Typically used from a receiver in an unidirectional communication channel, where the receiver just acknowledges receipt of data.
     * @see Protocol#send(Object) send(null)
     */
    public synchronized Map.Entry<Short, Packet<T>> send() {
        return send((T) null);
    }

    /**
     * Package the user-data, in order to transmit the user-data, acknowledge
     * received data and retransmit data (if {@link ProtocolConfig#setAutoRetransmit(boolean) retransmission is enabled}).
     * <br>
     * Zero or more {@link ProtocolListener#handleUnackedData(short, Object) unackedData} events
     * and zero or more {@link ProtocolListener#shouldRetransmit(short, Object) retransmit} events
     * may be fired before this method returns.
     *
     * @param data the user-data to package, if it's <code>null</code> no user-data will be contained in the package;
     *             the supplied user-data should not be modified afterwards by the user / application, as the data
     *             is referenced internally by protocol and used for retransmission later on;
     *             thus it's safest to clone the data before passing to this method
     * @return a <code>Map.Entry</code> containing the {@link Packet packaged user-data} and
     *          the <code>dataId</code> that was assigned to the user-data;
     *          the returned mapEntry should not be saved by the user,
     *          as its contents are invalidated next time one of the <code>send</code> methods is called
     */
    public synchronized Map.Entry<Short, Packet<T>> send(T data) {
        Packet<T> packet = controller.produce();
        sendDatas.clear();
        sendDatas.add(data);
        send(packet, sendDatas);

        sentPacketOut.packet = packet;
        sentPacketOut.id = data != null ? packet.getLastMetadata().getDataId() : null;
        return sentPacketOut;
    }

    /**
     * Package the user-datas, in order to transmit the user-datas, acknowledge
     * received data and retransmit data (if {@link ProtocolConfig#setAutoRetransmit(boolean) retransmission is enabled}).
     * <br>
     * Zero or more {@link ProtocolListener#handleUnackedData(short, Object) unackedData} events
     * and zero or more {@link ProtocolListener#shouldRetransmit(short, Object) retransmit} events
     * may be fired before this method returns.
     *
     * @param datas the user-datas to package, if it's <code>null</code> no user-data will be contained in the package;
     *             the supplied user-data should not be modified afterwards by the user / application, as the data
     *             is referenced internally by protocol and used for retransmission later on;
     *             thus it's safest to clone the data before passing to this method
     * @return a <code>NavigableMap<Short, Packet<T>></code> containing a single {@link Packet package of all user-datas}
     *          and the <code>dataIds</code> (in iteration order) that were assigned to the user-datas (in iteration order);
     *          the returned map should not be saved by the user,
     *          as its contents are invalidated next time one of the <code>send</code> methods is called
     */
    public synchronized NavigableMap<Short, Packet<T>> send(List<T> datas) {
        Packet<T> packet = controller.produce();
        send(packet, datas);

        sentPacketMap.clear();
        if (datas != null) {
            for (int i = 0, l = packet.getMetadatas().size(); i < l; ++i) {
                Metadata<T> metadata = packet.getMetadatas().get(i);
                if (datas.contains(metadata.getData()))
                    sentPacketMap.put(metadata.getDataId(), packet);
            }
        }
        return sentPacketMapOut;
    }

    private void send(Packet<T> packet, List<T> datas) {
        int dataSize = datas != null ? datas.size() : 0;
        if (dataSize > Packet.MAX_DATAS_PER_PACKET)
            throw new IndexOutOfBoundsException("Cannot add more than " + Packet.MAX_DATAS_PER_PACKET + " datas to packet!");

        List<Metadata<T>> retransmits = controller.retransmit();
        for (int i = 0, l = retransmits.size(); i < l && i + dataSize < Packet.MAX_DATAS_PER_PACKET; ++i)
            controller.send(packet, retransmits.get(i));

        if (datas != null) {
            for (int i = 0, l = datas.size(); i < l; ++i) {
                T data = datas.get(i);
                if (data != null)
                    controller.send(packet, controller.produce(data));
            }
        }
    }

    /**
     * Convenience method which writes the output of internally called {@link Protocol#send(Object) <code>send(data)</code>}
     * to a {@link java.io.ObjectOutput}.
     *
     * @param objectOutput the object output to write the packaged user-data to
     * @throws IOException  if there was an error writing to the <code>ObjectOutput</code>
     * @see Protocol#send(Object) <code>send(data)</code>
     */
    public synchronized Map.Entry<Short, Packet<T>> send(T data, ObjectOutput objectOutput) throws IOException {
        Map.Entry<Short, Packet<T>> packetEntry = send(data);
        Packet.<T>writeExternalStatic(packetEntry.getValue(), objectOutput);
        return packetEntry;
    }

    private final NavigableMap<Short, T> receivedDatas = new TreeMap<Short, T>(IdComparator.instance);
    private final NavigableMap<Short, T> receivedDatasOut = CollectionUtils.unmodifiableNavigableMap(receivedDatas);

    /**
     * Unpackage the packaged user-data, in order to retrieve the user-data that was received, acknowledge sent data and
     * receive retransmitted data (if {@link ProtocolConfig#setAutoRetransmit(boolean) retransmission is enabled}).
     * <br>
     * Zero or more {@link ProtocolListener#handleOrderedData(short, Object) orderedData},
     * {@link ProtocolListener#handleUnorderedData(short, Object) unorderedData} or
     * {@link ProtocolListener#handleAckedData(short, Object) ackedData} events may be fired before this method returns.
     *
     * @param packet the packaged-user data to unpackage
     * @return a <code>NavigableMap</code> containing all received (directly received or received by retransmission)
     *         user-datas and their assigned <code>dataId</code>s;
     *         the returned object should not be saved by the user,
     *         as its contents are invalidated next time one of the <code>receive</code> methods is called;
     *         the returned user-data should also not be modified afterwards by the user / application, as data is
     *         referenced internally by the protocol and used for informing proper receipt of ordered data later on;
     *         thus it's safest to clone the data received from this method
     */
    public synchronized NavigableMap<Short, T> receive(Packet<T> packet) {
        receivedDatas.clear();

        controller.consume(packet);
        Metadata<T> metadata = controller.receive(packet);
        while (metadata != null) {
            receivedDatas.put(metadata.getDataId(), controller.consume(metadata));
            metadata = controller.receive(packet);
        }

        return receivedDatasOut;
    }

    /**
     * Convenience method which reads the input of an {@link java.io.ObjectInput} with the help of
     * internally called {@link Protocol#receive(Packet) <code>receive(package)</code>}.
     *
     * @param objectInput the object input to read the packaged user-data from
     * @throws IOException if there was an error reading from the <code>ObjectInput</code>
     * @throws ClassNotFoundException if the object being read from the <code>ObjectInput</code> is not of the correct type
     * @see Protocol#send(Object) <code>receive(package)</code>
     */
    public synchronized NavigableMap<Short, T> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Packet<T> packet = Packet.<T>readExternalStatic(objectInput);
        return receive(packet);
    }

    /**
     * Compare <code>dataIds</code> against each other. The user <b>must not compare these ids</b> with built-in comparison
     * operators. These ids wrap around to their {@link Short#MIN_VALUE min value} once they are incremented beyond
     * their {@link Short#MAX_VALUE max value}, hence this compare method must be used.
     * @param dataId1 the first id to compare
     * @param dataId2 the second id to compare
     * @return {@inheritDoc}
     */
    @Override
    public int compare(Short dataId1, Short dataId2) {
        return IdComparator.instance.compare(dataId1, dataId2);
    }

    /**
     * Static version of the {@link #compare(Short, Short) instance compare} method.
     */
    public static int compare(short dataId1, short dataId2) {
        return IdComparator.instance.compare(dataId1, dataId2);
    }

    /**
     * Get the round-trip time. <br></br>
     * Updated on receiving acknowledgement of data receipt, using the formula
     * <code>RTT_SMOOTHED_AVG = 7/8 * RTT_SMOOTHED_AVG + 1/8 * RTT_NEW</code>
     * @return the exponentially smoothed round-trip time
     */
    public long getSmoothedRTT() {
        return controller.getSmoothedRTT();
    }

    /**
     * Get the round-trip time variance. <br></br>
     * Updated on receiving acknowledgement of data receipt, using the formula
     * <code> RTT_SMOOTHED_VAR = 3/4 * RTT_SMOOTHED_VAR + 1/4 * |RTT_SMOOTHED_AVG - RTT_NEW|</code>
     * @return the exponentially smoothed round-trip time variance
     */
    public long getRTTVariation() {
        return controller.getRTTVariation();
    }

    private static class PacketEntry<T> implements Map.Entry<Short, Packet<T>> {
        private Short id;
        private Packet<T> packet;

        private PacketEntry() {
        }


        @Override
        public Short getKey() {
            return id;
        }

        @Override
        public Packet<T> getValue() {
            return packet;
        }

        @Override
        public Packet<T> setValue(Packet<T> packet) {
            throw new UnsupportedOperationException();
        }
    }
}
