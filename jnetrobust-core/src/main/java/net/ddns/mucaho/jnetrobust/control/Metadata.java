package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.Freezable;
import net.ddns.mucaho.jnetrobust.util.IdComparator;
import net.ddns.mucaho.jnetrobust.util.Timestamp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;
import java.util.TreeSet;

public class Metadata<T> implements Timestamp, Freezable<Metadata<T>> {
    private transient long lastTouched = System.currentTimeMillis();

    private Short dataId;
    private NavigableSet<Short> transmissionIds =
            new TreeSet<Short>(IdComparator.instance);
    private transient NavigableSet<Short> transmissionIdsOut =
            CollectionUtils.unmodifiableNavigableSet(transmissionIds);

    private T value;

    public Metadata(Short dataId, T value) {
        this.dataId = dataId;
        this.value = value;
    }

    public Metadata() {
        super();
    }


    public Short getDataId() {
        return dataId;
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
        String out = "";
        out += "[";
        out += " ( " + dataId + " ) ";
        for (Short transmissionId : transmissionIds)
            out += transmissionId + " ";
        out += "]";
        out += ": " + (value != null ? value.toString() : "null");

        return out;
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
        transmissionIds.add(in.readShort());
        value = (T) in.readObject();
    }

    @Override
    public Metadata<T> clone() {
        Metadata<T> clone = new Metadata<T>();
        clone.value = value;
        clone.dataId = new Short(dataId);
        clone.transmissionIds.addAll(transmissionIds);
        return clone;
    }
}
