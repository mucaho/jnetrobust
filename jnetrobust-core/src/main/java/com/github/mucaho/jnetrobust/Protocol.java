/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.controller.PackagingController;
import com.github.mucaho.jnetrobust.controller.ProcessingController;
import com.github.mucaho.jnetrobust.controller.DebugProcessingController;
import com.github.mucaho.jnetrobust.controller.Packet;
import com.github.mucaho.jnetrobust.util.CollectionUtils;
import com.github.mucaho.jnetrobust.util.DebugProtocolListener;
import com.github.mucaho.jnetrobust.util.IdComparator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * The central API class that represents the virtual protocol.
 * It maintains the state of a virtual protocol instance and
 * offers methods to alter that state by wrapping user data with protocol specific metadata.
 * <p></p>
 * Various constructors are offered to instantiate a new protocol instance.
 * <p></p>
 * Typically you call the protocol's {@link Protocol#send(ByteBuffer)}
 * and {@link Protocol#receive(Packet)} methods in order to attach/detach protocol-specific information
 * to your user data. This protocol-specific information and the protocol's internal state is then used
 * to enable reliable & ordered communication even over an unreliable medium. <br></br>
 * Note that both {@link Protocol#send(ByteBuffer)} and {@link Protocol#receive(Packet)} methods may trigger zero or more
 * {@link ProtocolListener listener} events before they return. <br></br>
 * The {@link Protocol#send(Packet, java.io.ObjectOutput)} & {@link Protocol#receive(java.io.ObjectInput)}
 * are utility methods write the packaged user-data to a {@link java.io.ObjectOutput} or
 * read the packaged user-data from a {@link java.io.ObjectInput} respectively. Other than that, they behave
 * exactly like the {@link Protocol#send(ByteBuffer)} and {@link Protocol#receive(Packet)} methods. <br></br>
 * How serialization is done and over which medium the {@link Packet packaged user-data} is sent is
 * up to the user (analogue for receiving and deserialization).
 * <p></p>
 * Note that always the <b>same two protocol instances must communicate with each other</b>, as they share a distributed
 * state together. <br></br>
 * In order for the protocol to work the user <b>must immediately acknowledge received data</b>, either by sending
 * an empty transmission or sending new user-data if it is available. The user may delay doing this, if he
 * is sending new data at a <b>"slower" side fixed interval (ms) &lt; "faster" side fixed interval * 5 (ms)</b>.
 * Note that the congestion control only works if both sides send at the same periodic interval.<br></br>
 * <p></p>
 * This class also offer utility methods to query the {@link Protocol#getSmoothedRTT() round-trip time}, as well
 * as the {@link Protocol#getRTTVariation() round-trip time variation}. <p></p>
 * This class also offers the ability to {@link Protocol#compare(Short, Short) compare <code>dataIds</code>} against each other. The user <b>must not compare these ids</b> with built-in comparison
 * operators. These ids wrap around to their {@link Short#MIN_VALUE min value} once they are incremented beyond
 * their {@link Short#MAX_VALUE max value}, hence this compare method must be used.
 */
public class Protocol implements Comparator<Short> {
    private final PackagingController controller;
    // TODO: possibly add currentlyInUse boolean to prevent reentering send / receive procedure while listeners fire

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener listener) {
        this(listener, new ProtocolConfig(), null);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolConfig config) {
        this(new ProtocolListener(), config, null);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(Logger logger) {
        this(new ProtocolListener(), new ProtocolConfig(), logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolConfig config, Logger logger) {
        this(new ProtocolListener(), config, logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener listener, Logger logger) {
        this(listener, new ProtocolConfig(), logger);
    }

    /**
     * Convenience constructor that wraps the {@link #Protocol(ProtocolListener, ProtocolConfig, Logger) "default" constructor}.
     * If not provided, the default {@link ProtocolListener}, {@link ProtocolConfig} and no {@link Logger} are used.
     */
    public Protocol(ProtocolListener listener, ProtocolConfig config) {
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
    public Protocol(ProtocolListener listener, ProtocolConfig config, Logger logger) {
        if (listener == null) listener = new ProtocolListener();
        if (config == null) config = new ProtocolConfig();

        if (logger != null) {
            ProtocolListener debugListener = new DebugProtocolListener(listener, logger);
            this.controller = new PackagingController(new DebugProcessingController(debugListener, config, logger));
        } else {
            this.controller = new PackagingController(new ProcessingController(listener, config));
        }
    }

    private final ByteBuffer dataIn = ByteBuffer.allocate(ProtocolConfig.getHighestPossibleMTUSize());

    /**
     * Convenience method does the same as {@link Protocol#send(ByteBuffer) <code>send(null)</code>}.
     * Typically used from a receiver in an unidirectional communication channel, where the receiver just acknowledges receipt of data.
     * @see Protocol#send(ByteBuffer) send(null)
     */
    public synchronized NavigableMap<Short, Packet> send() {
        return send((ByteBuffer) null);
    }

    /**
     * Convenience method does the same as {@link Protocol#send(ByteBuffer) <code>send(data)</code>},
     * by wrapping the supplied {@code data} in a temporary {@code ByteBuffer}.
     * @see Protocol#send(ByteBuffer) send(data)
     */
    public synchronized NavigableMap<Short, Packet> send(byte[] data) {
        return send(data, 0, data.length);
    }

    /**
     * Convenience method does the same as {@link Protocol#send(ByteBuffer) <code>send(data)</code>},
     * by wrapping the supplied {@code data} in a temporary {@code ByteBuffer} starting from the specified {@code offset}
     * and stretching for {@code length}.
     * @see Protocol#send(ByteBuffer) send(data)
     */
    public synchronized NavigableMap<Short, Packet> send(byte[] data, int offset, int length) {
        dataIn.clear();
        dataIn.put(data, offset, length);
        dataIn.flip();

        return send(dataIn);
    }

    /**
     * Package the user-datas, in order to transmit the user-datas, acknowledge
     * received data and retransmit data (if {@link ProtocolConfig#getAutoRetransmitMode() automatic retransmission} is enabled).
     * <br />
     * Zero or more {@link ProtocolListener#handleUnackedData(short, ByteBuffer) unackedData} events
     * and zero or more {@link ProtocolListener#shouldRetransmit(short, ByteBuffer) retransmit} events
     * may be fired before this method returns.
     * <br /><br />
     * The supplied data must be smaller or equal to the {@link #getMaximumDataSize() maximum data size} allowed for a
     * single package. Otherwise a {@link IllegalArgumentException} is thrown. See that method for further information.
     * <br />
     * This also means that each of the returned packages <b>should be transmitted individually</b> over the underlying
     * transport protocol (e.g. UDP).
     * <br /><br />
     * Note that the returned {@code NavigableMap} may have a deliberate {@code null} as its only key.
     * This situation occurs in an unidirectional communication setup, where the receiver just returns empty packets
     * acknowledging the receipt of data. As this empty {@code packet} contains no packaged user-data, there is no
     * proper key to associate the packet to, hence why the {@code null} key may occur.
     * The returned {@code NavigableMap} can thus be navigated safely and quickly in the following fashion:
     * <pre>
     * Short key = packetMap.isEmpty() ? null : packetMap.firstKey();
     * boolean currentKeyIsOkToBeNull = !packetMap.isEmpty() && packetMap.firstKey() == null;
     * while (key != null || currentKeyIsOkToBeNull) {
     *     Packet packet = packetMap.get(key);
     *     // send the packet
     *
     *     key = packetMap.higherKey(key);
     *     currentKeyIsOkToBeNull = false;
     * }
     * </pre>
     *
     * @param data the user-data to package, if it's <code>null</code> no user-data will be contained in the package;
     * @return a <code>NavigableMap</code> mapping all {@code dataIds} to their respective {@link Packet package of user-data};
     *          the supplied user-data is contained in the package of the highest entry in the {@code NavigableMap}, if the user-data was provided;
     *          additionally, the map is filled with previous packages that need to be retransmitted;
     *          note that if no user-data was provided (and no automatic / manual retransmits are triggered)
     *          the returned map may have just one key, which is null, mapping an empty packet containing just the
     *          to-be-sent acknowledgements, see the the above code for proper and fast iteration;
     *          the returned map should not be saved by the user,
     *          as its contents are invalidated next time one of the <code>send</code> methods is called
     * @throws IllegalArgumentException if the supplied {@code data} is larger than the
     *                                  {@link #getMaximumDataSize() maximum data size} allowed
     */
    public synchronized NavigableMap<Short, Packet> send(ByteBuffer data) {
        return controller.send(data);
    }

    /**
     * Convenience method which can be used to iteratively write the output of
     * {@link Protocol#send(ByteBuffer) <code>send(data)</code>} to a {@link java.io.ObjectOutput}.
     * <br />
     * Note that each {@code packet} is meant to be sent separately over the underlying communication channel.
     *
     * @param objectOutput the object output to write the packaged user-data to
     * @throws IOException  if there was an error writing to the <code>ObjectOutput</code>
     * @see Protocol#send(ByteBuffer) <code>send(data)</code>
     */
    public synchronized void send(Packet packet, ObjectOutput objectOutput) throws IOException {
        controller.send(packet, objectOutput);
    }

    /**
     * Unpackage the packaged user-data, in order to retrieve the user-data that was received, acknowledge sent data and
     * receive retransmitted data (if {@link ProtocolConfig#getAutoRetransmitMode() automatic retransmission} is enabled).
     * <br />
     * Zero or more {@link ProtocolListener#handleOrderedData(short, ByteBuffer) orderedData},
     * {@link ProtocolListener#handleUnorderedData(short, ByteBuffer) unorderedData},
     * {@link ProtocolListener#handleAckedData(short, ByteBuffer) ackedData} or
     * {@link ProtocolListener#handleNewestData(short, ByteBuffer) newestData}
     * events may be fired before this method returns.
     *
     * @param packet the packaged-user data to unpackage
     * @return a <code>NavigableMap</code> containing all received (directly received or received by retransmission)
     *         user-datas and their assigned <code>dataId</code>s;
     *         the returned map should not be saved by the user,
     *         as its contents are invalidated next time one of the <code>receive</code> methods is called;
     *         the returned user-data should be processed, copied or cloned by the user immediately,
     *         as the data gets invalidated next time one of the <code>receive</code> methods is called;
     */
    public synchronized NavigableMap<Short, ByteBuffer> receive(Packet packet) {
        return controller.receive(packet);
    }

    /**
     * Convenience method which reads the input of an {@link java.io.ObjectInput} with the help of
     * internally called {@link Protocol#receive(Packet) <code>receive(package)</code>}.
     *
     * @param objectInput the object input to read the packaged user-data from
     * @throws IOException if there was an error reading from the <code>ObjectInput</code>
     * @throws ClassNotFoundException if the object being read from the <code>ObjectInput</code> is not of the correct type
     * @see Protocol#send(ByteBuffer) <code>receive(package)</code>
     */
    public synchronized NavigableMap<Short, ByteBuffer> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        return controller.receive(objectInput);
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

    /**
     * Gets the path's maximum transmission unit size in bytes. Defaults to {@link ProtocolConfig#CONSERVATIVE_MTU_SIZE}.
     * <br />
     * The protocol instance uses the MTU to limit the size of emitted packets from the protocol
     * to prevent packet fragmentation on the Internet Layer.
     * <br />
     * The MTU size directly impacts {@link #getMaximumDataSize() the amount of data the user is allowed to send in one packet}.
     * The user may still split the data manually into multiple chunks which result in multiple packets
     * that can be send over the underlying transport protocol (e.g. UDP).
     * <br /><br />
     * It is initially set to a conservative value, however the user may set this in a majority of cases
     * to {@link ProtocolConfig#MOST_COMMON_MTU_SIZE}.
     * Furthermore, path MTU discovery may be done to determine the actual MTU size between sender
     * and receiver protocol instances.
     * <br /><br />
     * The closer this setting is to the real path's MTU (without going over it of course),
     * the more data can be sent with less "metadata" / header overhead.
     * <br /><br />
     * The provided value can not be greater than the
     * {@link ProtocolConfig#getHighestPossibleMTUSize() highest possible MTU size} and is therefore clamped accordingly.
     */
    public int getMaximumTransmissionUnitSize() {
        return controller.getMaximumTransmissionUnitSize();
    }

    /**
     * Sets the path's maximum transmission unit size in bytes. Defaults to {@link ProtocolConfig#CONSERVATIVE_MTU_SIZE}.
     * <br />
     * The protocol instance uses the MTU to limit the size of emitted packets from the protocol
     * to prevent packet fragmentation on the Internet Layer.
     * <br />
     * The MTU size directly impacts {@link #getMaximumDataSize() the amount of data the user is allowed to send in one packet}.
     * The user may still split the data manually into multiple chunks which result in multiple packets
     * that can be send over the underlying transport protocol (e.g. UDP).
     * <br /><br />
     * It is initially set to a conservative value, however the user may set this in a majority of cases
     * to {@link ProtocolConfig#MOST_COMMON_MTU_SIZE}.
     * Furthermore, path MTU discovery may be done to determine the actual MTU size between sender
     * and receiver protocol instances.
     * <br /><br />
     * The closer this setting is to the real path's MTU (without going over it of course),
     * the more data can be sent with less "metadata" / header overhead.
     * <br /><br />
     * The provided value can not be greater than the
     * {@link ProtocolConfig#getHighestPossibleMTUSize() highest possible MTU size} and is therefore clamped accordingly.
     */
    public void setMaximumTransmissionUnitSize(int maximumTransmissionUnitSize) {
        controller.setMaximumTransmissionUnitSize(maximumTransmissionUnitSize);
    }

    /**
     * Gets the maximum data size which regulates how much user-data may be packaged into a single packet
     * that gets transmitted over the network. <br /><br />
     * When sending new data via the protocol instance, the data <b>must not</b> exceed this limit, otherwise an
     * exception is thrown. <br />
     * Alternatively, the user may split the data into smaller, logical chunks < {@code maximumDataSize} and invoke the
     * send method for each of those chunks. This constraint is typically encountered when trying to send a serialized
     * collection of objects, which can be solved by sending only parts of such collection with each packet
     * and optionally tagging all these objects with a collection identifier, tying them all together at the receiver side.
     * <br />
     * The protocol does not do this automatically, as it has no knowledge about the objects and their boundaries
     * in this serialized format.
     * <br /><br />
     * This limitation is enforced in order to avoid IP fragmentation, which lowers transmission performance considerably
     * in congested networks.
     *
     * @see #setMaximumTransmissionUnitSize(int)
     */
    public int getMaximumDataSize() {
        return controller.getMaximumDataSize();
    }
}
