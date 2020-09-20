/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.control.Segment;
import com.github.mucaho.jnetrobust.util.BitConstants;
import com.github.mucaho.jnetrobust.util.Freezable;
import com.github.mucaho.jnetrobust.util.Sizeable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public final class Packet implements Freezable<Packet>, Sizeable {
    public static final transient int MAX_DATAS_PER_PACKET = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) - 1;

    public Packet() {
        super();
    }

    // possibly use ArrayDeque with iterator instead
    private final LinkedList<Segment> segments = new LinkedList<Segment>();
    private transient final List<Segment> segmentsOut = Collections.unmodifiableList(segments);
    private Short transmissionAck;
    private long precedingTransmissionAcks;

    public List<Segment> getSegments() {
        return segmentsOut;
    }

    public Segment getFirstSegment() {
        return segments.peekFirst();
    }

    public Segment getLastSegment() {
        return segments.peekLast();
    }

    void addLastSegment(Segment segment) {
        if (segments.size() >= MAX_DATAS_PER_PACKET)
            throw new IndexOutOfBoundsException("Cannot add more than " + MAX_DATAS_PER_PACKET + " segments to packet!");

        segments.addLast(segment);
    }

    Segment removeFirstSegment() {
        return segments.pollFirst();
    }

    public Short getTransmissionAck() {
        return transmissionAck;
    }

    void setTransmissionAck(Short ack) {
        this.transmissionAck = ack;
    }

    public long getPrecedingTransmissionAcks() {
        return precedingTransmissionAcks;
    }

    void setPrecedingTransmissionAcks(long lastAcks) {
        this.precedingTransmissionAcks = lastAcks;
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    public String toDebugString() {
        return "Packet:" + "\t"
                + "transmissionAck = " + transmissionAck + "\t"
                + "precedingTransmissionAcks = " + String.format("%33s", Long.toBinaryString(precedingTransmissionAcks)) + "\t"
                + "segments = " + Arrays.deepToString(segments.toArray()) + "\n";
    }

    /**
     * Externalize the packet.
     * Static method that does the same thing as {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} .
     *
     * @param packet the instance to write
     * @param out    the {@link java.io.ObjectOutput} to write to
     * @throws IOException if an error occurs
     */
    public static void writeExternalStatic(Packet packet, ObjectOutput out) throws IOException {
        packet.writeExternal(out);
    }

    /**
     * Deexternalize the packet.
     * Static method that does the same thing as {@link java.io.Externalizable#readExternal(java.io.ObjectInput)} .
     *
     * @param in the {@link java.io.ObjectInput} to read from
     * @return a new protocolUnit instance, constructed by the data read
     * @throws IOException            if an error occurs
     * @throws ClassNotFoundException if an error occurs.
     */
    public static Packet readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        Packet packet = new Packet();
        packet.readExternal(in);
        return packet;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        writeExternal(out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(transmissionAck);

        if (ProtocolConfig.useExtendedPrecedingTransmissionAcks())
            out.writeLong(precedingTransmissionAcks);
        else
            out.writeInt(BitConstants.convertBits(precedingTransmissionAcks));

        out.writeByte(segments.size());
        for (int i = 0, l = segments.size(); i < l; ++i)
            Segment.writeExternalStatic(segments.get(i), out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        transmissionAck = in.readShort();

        if (ProtocolConfig.useExtendedPrecedingTransmissionAcks())
            precedingTransmissionAcks = in.readLong();
        else
            precedingTransmissionAcks = BitConstants.convertBits(in.readInt());

        int size = in.readUnsignedByte();
        for (int i = 0; i < size; ++i)
            segments.addLast(Segment.readExternalStatic(in));
    }

    @Override
    public Packet clone() {
        Packet clone = new Packet();
        clone.transmissionAck = transmissionAck;
        clone.precedingTransmissionAcks = precedingTransmissionAcks;
        for (int i = 0, l = segments.size(); i < l; ++i)
            clone.addLastSegment(segments.get(i).clone());
        return clone;
    }

    // TODO: test getSize() == serializationSize
    @Override
    public int getSize() {
        int size = Short.SIZE / Byte.SIZE // transmissionAck
                // precedingTransmissionAcks
                + (ProtocolConfig.useExtendedPrecedingTransmissionAcks() ? Long.SIZE / Byte.SIZE : Integer.SIZE / Byte.SIZE)
                + Byte.SIZE / Byte.SIZE; // segmentsSize
        for (int i = 0, l = segments.size(); i < l; ++i)
            size += segments.get(i).getSize(); // segments
        return size;
    }
}
