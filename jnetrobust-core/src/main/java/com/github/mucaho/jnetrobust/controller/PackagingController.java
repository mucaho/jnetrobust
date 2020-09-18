/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.util.CollectionUtils;
import com.github.mucaho.jnetrobust.util.IdComparator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PackagingController {
    private final ProcessingController controller;

    public PackagingController(ProcessingController controller) {
        this.controller = controller;

        setMaximumTransmissionUnitSize(maximumTransmissionUnitSize);
    }

    private int maximumTransmissionUnitSize = ProtocolConfig.CONSERVATIVE_MTU_SIZE;
    private int maximumPacketSize;
    private int minimumPacketSize;
    private int maximumDataSize;

    public void setMaximumTransmissionUnitSize(int maximumTransmissionUnitSize) {
        this.maximumTransmissionUnitSize = Math.min(maximumTransmissionUnitSize, ProtocolConfig.getHighestPossibleMTUSize());

        minimumPacketSize = new Packet().getSize();
        maximumPacketSize = maximumTransmissionUnitSize
                - ProtocolConfig.MAXIMUM_LOWER_STACK_HEADER_SIZE;
        maximumDataSize = maximumPacketSize
                - minimumPacketSize
                - new Segment().getSize();
    }

    public int getMaximumTransmissionUnitSize() {
        return maximumTransmissionUnitSize;
    }

    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    public int getMinimumPacketSize() {
        return minimumPacketSize;
    }

    public int getMaximumDataSize() {
        return maximumDataSize;
    }

    private final List<Segment> allSegments = new ArrayList<Segment>();
    private final List<Segment> packetSegments = new ArrayList<Segment>();
    private final NavigableMap<Short, Packet> packetMap = new TreeMap<Short, Packet>(IdComparator.instance);
    private final NavigableMap<Short, Packet> packetMapOut = CollectionUtils.unmodifiableNavigableMap(packetMap);

    public NavigableMap<Short, Packet> send(ByteBuffer data) {
        int dataSize = data != null ? data.limit() : 0;
        if (dataSize > getMaximumDataSize())
            throw new IllegalArgumentException("Cannot add more than " + getMaximumDataSize() + " data bytes to packet!");

        packetMap.clear();

        // produce all retransmits & user-data segments
        allSegments.clear();
        allSegments.addAll(controller.retransmit());
        if (data != null)
            allSegments.add(controller.produce(data));

        // create new segment set
        int currentPacketSize = minimumPacketSize;
        packetSegments.clear();

        for (int i = 0, l = allSegments.size(); i < l; ++i) {
            Segment segment = allSegments.get(i);
            int segmentSize = segment.getSize();

            if ((currentPacketSize + segmentSize > maximumPacketSize) || (packetSegments.size() >= Packet.MAX_DATAS_PER_PACKET)) {
                // apply current segment set to new packet
                doSend(packetSegments, packetMap);

                // create new packet
                currentPacketSize = minimumPacketSize;
                packetSegments.clear();
            }

            // increase current segment set
            currentPacketSize += segmentSize;
            packetSegments.add(segment);
        }
        if (!packetSegments.isEmpty()) {
            // apply remaining segments to new packet
            doSend(packetSegments, packetMap);
        }

        // if there is no packet, send at least an empty packet
        if (packetMap.isEmpty()) {
            System.err.println("EMPTY");
            packetMap.put(null, controller.produce());
        }

        return packetMapOut;
    }

    private void doSend(List<Segment> segments, NavigableMap<Short, Packet> outMap) {
        Packet packet = controller.produce();
        controller.send(packet, segments);
        if (!packet.getSegments().isEmpty()) // check for retroactive discards
            outMap.put(packet.getLastSegment().getDataId(), packet);
        else
            System.err.println("DISCARD");
    }

    public void send(Packet packet, ObjectOutput objectOutput) throws IOException {
        Packet.writeExternalStatic(packet, objectOutput);
    }

    private final NavigableMap<Short, ByteBuffer> receivedDatas = new TreeMap<Short, ByteBuffer>(IdComparator.instance);
    private final NavigableMap<Short, ByteBuffer> receivedDatasOut = CollectionUtils.unmodifiableNavigableMap(receivedDatas);

    public NavigableMap<Short, ByteBuffer> receive(Packet packet) {
        receivedDatas.clear();

        controller.consume(packet);
        Segment segment = controller.receive(packet);
        while (segment != null) {
            receivedDatas.put(segment.getDataId(), controller.consume(segment));
            segment = controller.receive(packet);
        }

        return receivedDatasOut;
    }

    public NavigableMap<Short, ByteBuffer> receive(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Packet packet = Packet.readExternalStatic(objectInput);
        return receive(packet);
    }

    public long getSmoothedRTT() {
        return controller.getSmoothedRTT();
    }
    public long getRTTVariation() {
        return controller.getRTTVariation();
    }
}
