/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.CollectionUtils;
import com.github.mucaho.jnetrobust.util.Freezable;
import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.Timestamp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

public class Metadata<T> implements Timestamp, Freezable<Metadata<T>> {
    private transient long lastTouched = System.currentTimeMillis();

    private Short dataId;
    private final transient NavigableSet<Short> dataIds = new TreeSet<Short>();
    private final transient NavigableSet<Short> dataIdsOut = CollectionUtils.unmodifiableNavigableSet(dataIds);

    private final NavigableSet<Short> transmissionIds = new TreeSet<Short>(IdComparator.instance);
    private final transient NavigableSet<Short> transmissionIdsOut = CollectionUtils.unmodifiableNavigableSet(transmissionIds);

    private T value;

    public Metadata(Short dataId, T value) {
        this.dataId = dataId;
        this.dataIds.add(dataId);
        this.value = value;
    }

    public Metadata() {
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


    public T getData() {
        return value;
    }


    void updateTime() {
        lastTouched = System.currentTimeMillis();
    }

    @Override
    public long getTime() {
        return lastTouched;
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
        out.append(": ").append(value != null ? value.toString() : "null");

        return out.toString();
    }

    /**
     * Externalize the metadata.
     * Static method that does the same thing as {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} .
     *
     * @param metadata the instance to write
     * @param out           the {@link java.io.ObjectOutput} to write to
     * @throws IOException if an error occurs
     */
    public static <T> void writeExternalStatic(Metadata<T> metadata, ObjectOutput out) throws IOException {
        metadata.writeExternal(out);
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
    public static <T> Metadata<T> readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        Metadata<T> metadata = new Metadata<T>();
        metadata.readExternal(in);
        return metadata;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(dataId);
        out.writeShort(transmissionIds.last());
        out.writeObject(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dataId = in.readShort();
        dataIds.add(dataId);
        transmissionIds.add(in.readShort());
        value = (T) in.readObject();
    }

    @Override
    public Metadata<T> clone() {
        Metadata<T> clone = new Metadata<T>();
        clone.value = value;
        clone.dataId = new Short(dataId);
        clone.dataIds.add(dataId);
        clone.transmissionIds.addAll(transmissionIds);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadata<?> metadata = (Metadata<?>) o;

        return dataId != null ? dataId.equals(metadata.dataId) : metadata.dataId == null;
    }

    @Override
    public int hashCode() {
        return dataId != null ? dataId.hashCode() : 0;
    }
}
