/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.ProtocolConfig;
import com.github.mucaho.jnetrobust.util.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.*;

public final class Segment implements Timestamp, Freezable<Segment>, Sizeable {
    private transient long newestTransmissionTime = System.currentTimeMillis();

    private Short dataId;
    private final transient NavigableSet<Short> dataIds = new TreeSet<Short>();
    private final transient NavigableSet<Short> dataIdsOut = CollectionUtils.unmodifiableNavigableSet(dataIds);

    private final NavigableSet<Short> transmissionIds = new TreeSet<Short>(IdComparator.instance);
    private final transient NavigableSet<Short> transmissionIdsOut = CollectionUtils.unmodifiableNavigableSet(transmissionIds);

    private final ByteBuffer data = ByteBuffer.allocate(ProtocolConfig.getHighestPossibleMTUSize());
    private final transient ByteBuffer dataOut = data.asReadOnlyBuffer();

    public Segment(Short dataId, ByteBuffer data) {
        this.dataId = dataId;
        this.dataIds.add(dataId);
        if (data != null) {
            this.data.put(data);
            this.dataOut.position(this.data.position());
            this.data.flip();
            this.dataOut.flip();
        }
    }

    public Segment() {
        super();
    }

    public Short getDataId() {
        return dataId;
    }

    public NavigableSet<Short> getDataIds() {
        return dataIdsOut;
    }

    boolean addTransmissionId(Short e) {
        return transmissionIds.add(e);
    }

    boolean removeTransmissionId(Short e) {
        return transmissionIds.remove(e);
    }

    void clearTransmissionIds() {
        transmissionIds.clear();
    }

    public NavigableSet<Short> getTransmissionIds() {
        return transmissionIdsOut;
    }

    public Short getFirstTransmissionId() {
        return transmissionIds.isEmpty() ? null : transmissionIds.first();
    }

    public Short getLastTransmissionId() {
        return transmissionIds.isEmpty() ? null : transmissionIds.last();
    }

    public ByteBuffer getData() {
        if (ByteBufferUtils.getDataSize(data) > 0) {
            if (!dataOut.hasRemaining())
                dataOut.rewind();
            return dataOut;
        } else {
            return null;
        }
    }

    void updateTime() {
        newestTransmissionTime = System.currentTimeMillis();
    }

    @Override
    public long getTime() {
        return newestTransmissionTime;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("[");
        out.append(" ( ").append(dataId).append(" ) ");
        Short transmissionId = transmissionIds.isEmpty() ? null : transmissionIds.first();
        while (transmissionId != null) {
            out.append(transmissionId).append(" ");
            transmissionId = transmissionIds.higher(transmissionId);
        }
        out.append("]");
        out.append(": ").append(ByteBufferUtils.getDataSize(data)).append("B");

        return out.toString();
    }

    /**
     * Externalize the segment.
     * Static method that does the same thing as {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} .
     *
     * @param segment the instance to write
     * @param out           the {@link java.io.ObjectOutput} to write to
     * @throws IOException if an error occurs
     */
    public static void writeExternalStatic(Segment segment, ObjectOutput out) throws IOException {
        segment.writeExternal(out);
    }

    /**
     * Deexternalize the multiKeyValue.
     * Static method that does the same thing as {@link java.io.Externalizable#readExternal(java.io.ObjectInput)} .
     *
     * @param in the {@link java.io.ObjectInput} to read from
     * @return a new multiKeyValue instance, constructed by the data read
     * @throws IOException            if an error occurs
     * @throws ClassNotFoundException if an error occurs.
     */
    public static Segment readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        Segment segment = new Segment();
        segment.readExternal(in);
        return segment;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        writeExternal(out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(dataId);
        out.writeShort(transmissionIds.last());
        int dataSize = ByteBufferUtils.getDataSize(data);
        out.writeInt(dataSize);
        if (dataSize > 0)
            out.write(data.array(), data.arrayOffset(), data.limit());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dataId = in.readShort();
        dataIds.add(dataId);
        transmissionIds.add(in.readShort());
        int dataSize = in.readInt();
        if (dataSize > 0) {
            in.read(data.array(), 0, dataSize);
            data.limit(dataSize);
            dataOut.limit(dataSize);
        }
    }

    @Override
    public Segment clone() {
        Segment clone = new Segment();
        if (getData() != null) getData().rewind();
        clone.data.put(getData());
        clone.dataOut.position(clone.data.position());
        clone.data.flip();
        clone.dataOut.flip();
        clone.dataId = dataId;
        clone.dataIds.add(dataId);
        clone.transmissionIds.addAll(transmissionIds);
        return clone;
    }

    @Override
    public int getSize() {
        return Short.SIZE / Byte.SIZE // dataId
                + Short.SIZE / Byte.SIZE // lastTransmissionId
                + Integer.SIZE / Byte.SIZE // dataLimit
                + ByteBufferUtils.getDataSize(data); // data
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Segment segment = (Segment) o;

        return dataId != null ? dataId.equals(segment.dataId) : segment.dataId == null;
    }

    @Override
    public int hashCode() {
        return dataId != null ? dataId.hashCode() : 0;
    }
}
