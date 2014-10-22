/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.controller.DebugController;
import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.controller.RetransmissionController;
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
    private final RetransmissionController<T> controller;
    private final boolean shouldRetransmit;

    /**
     * Construct a new protocol instance using the default {@link ProtocolConfig protocol configuration}.
     * @param protocolListener  the <code>ProtocolListener</code> which will be notified about protocol events
     */
    public Protocol(ProtocolListener<T> protocolListener) {
        this(protocolListener, null);
    }

    /**
     * Construct a new protocol instance using the supplied {@link ProtocolConfig protocol configuration}.
     * @param config    the <code>protocol configuration</code> which will be used to configure this protocol instance
     */
    public Protocol(ProtocolConfig<T> config) {
        this(config, null);
    }

    /**
     * Construct a new protocol instance using the default {@link ProtocolConfig protocol configuration}.
     * Internal state changes will be logged using the supplied {@link Logger logger}.
     * @param protocolListener  the <code>ProtocolListener</code> which will be notified about protocol events
     * @param logger    the <code>Logger</code> which will be used to track internal state changes
     */
    public Protocol(ProtocolListener<T> protocolListener, Logger logger) {
        this(new ProtocolConfig<T>(protocolListener), logger);
    }

    /**
     * Construct a new protocol instance using the supplied {@link ProtocolConfig protocol configuration}.
     * Internal state changes will be logged using the supplied {@link Logger logger}.
     * @param config    the <code>protocol configuration</code> which will be used to configure this protocol instance
     * @param logger    the <code>Logger</code> which will be used to track internal state changes
     */
    public Protocol(ProtocolConfig<T> config, Logger logger) {
        this.shouldRetransmit = config.shouldRetransmit();
        if (logger != null) {
            ProtocolListener<T> debugListener = new DebugProtocolListener<T>(config.listener, logger);
            this.controller = new DebugController<T>(new ProtocolConfig<T>(debugListener, config), logger);
        } else {
            this.controller = new RetransmissionController<T>(config);
        }

    }




    private final PacketEntry<T> sentPacketOut = new PacketEntry<T>();

    /**
     * Convenience method does the same as {@link Protocol#send(Object) <code>send(null)</code>}.
     * @see Protocol#send(Object) send(null)
     */
    public synchronized Map.Entry<Short, Packet<T>> send() {
        return send(null);
    }

    /**
     * Package the user-data, in order to transmit the user-data, acknowledge
     * received data and retransmit data (if {@link ProtocolConfig#setShouldRetransmit(boolean) retransmission is enabled}).
     * <br>
     * Zero or more {@link ProtocolListener#handleUnackedData(short, Object) unackedData} events may be fired before
     * this method returns.
     *
     * @param data the user-data to package, if it's <code>null</code> no user-data will be contained in the package
     * @return a <code>Map.Entry</code> containing the {@link Packet packaged user-data} and
     *          the <code>dataId</code> that was assigned to the user-data;
     *          the returned mapEntry should not be saved by the user,
     *          as its contents are invalidated next time one of the <code>send</code> methods is called
     */
    public synchronized Map.Entry<Short, Packet<T>> send(T data) {
        Packet<T> packet = controller.produce();
        if (shouldRetransmit) {
            Collection<Metadata<T>> retransmits = controller.retransmit();
            for (Metadata<T> retransmit : retransmits) {
                controller.send(packet, retransmit);
            }
        }
        if (data != null)
            controller.send(packet, controller.produce(data));

        sentPacketOut.packet = packet;
        sentPacketOut.id = data != null ? packet.getLastMetadata().getDataId() : null;
        return sentPacketOut;
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
     * receive retransmitted data (if {@link ProtocolConfig#setShouldRetransmit(boolean) retransmission is enabled}).
     * <br>
     * Zero or more {@link ProtocolListener#handleOrderedData(short, Object) orderedData},
     * {@link ProtocolListener#handleUnorderedData(short, Object) unorderedData} or
     * {@link ProtocolListener#handleAckedData(short, Object) ackedData} events may be fired before this method returns.
     *
     * @param packet the packaged-user data to unpackage
     * @return a <code>NavigableMap</code> containing all received (directly received or received by retransmission)
     *         user-datas and their assigned <code>dataId</code>s;
     *         the returned object should not be saved by the user,
     *         as its contents are invalidated next time one of the <code>receive</code> methods is called
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
