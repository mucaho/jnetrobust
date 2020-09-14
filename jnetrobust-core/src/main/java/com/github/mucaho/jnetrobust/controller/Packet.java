/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.controller;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.control.Metadata;
import com.github.mucaho.jnetrobust.util.BitConstants;
import com.github.mucaho.jnetrobust.util.Freezable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public final class Packet<T> implements Freezable<Packet<T>> {
    public static final transient int MAX_DATAS_PER_PACKET = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) - 1;

    public Packet() {
        super();
    }

    private LinkedList<Metadata<T>> metadatas = new LinkedList<Metadata<T>>();
    private transient List<Metadata<T>> metadatasOut = Collections.unmodifiableList(metadatas);
    private short transmissionAck;
    private long precedingTransmissionAcks;

    public List<Metadata<T>> getMetadatas() {
        return metadatasOut;
    }

    public Metadata<T> getFirstMetadata() {
        return metadatas.peekFirst();
    }

    public Metadata<T> getLastMetadata() {
        return metadatas.peekLast();
    }

    void addLastMetadata(Metadata<T> metadata) {
        if (metadatas.size() >= MAX_DATAS_PER_PACKET)
            throw new IndexOutOfBoundsException("Cannot add more than " + MAX_DATAS_PER_PACKET + " metadatas to packet.");

        metadatas.addLast(metadata);
    }

    Metadata<T> removeFirstMetadata() {
        return metadatas.pollFirst();
    }

    Metadata<T> removeLastMetadata() {
        return metadatas.pollLast();
    }

    Metadata<T> remove(int i) {
        return metadatas.remove(i);
    }

    public short getTransmissionAck() {
        return transmissionAck;
    }

    void setTransmissionAck(short ack) {
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
                + "metadatas = " + Arrays.deepToString(metadatas.toArray()) + "\n";
    }

    /**
     * Externalize the packet.
     * Static method that does the same thing as {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} .
     *
     * @param packet the instance to write
     * @param out    the {@link java.io.ObjectOutput} to write to
     * @throws IOException if an error occurs
     */
    public static <T> void writeExternalStatic(Packet<T> packet, ObjectOutput out) throws IOException {
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
    public static <T> Packet<T> readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        Packet<T> packet = new Packet<T>();
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

        out.writeByte(metadatas.size());
        for (int i = 0, l = metadatas.size(); i < l; ++i)
            Metadata.<T>writeExternalStatic(metadatas.get(i), out);
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
            metadatas.addLast(Metadata.<T>readExternalStatic(in));
    }

    @Override
    public Packet<T> clone() {
        Packet<T> clone = new Packet<T>();
        clone.transmissionAck = transmissionAck;
        clone.precedingTransmissionAcks = precedingTransmissionAcks;
        for (int i = 0, l = metadatas.size(); i < l; ++i)
            clone.addLastMetadata(metadatas.get(i).clone());
        return clone;
    }
}
