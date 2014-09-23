package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.Freezable;
import net.ddns.mucaho.jnetrobust.util.SequenceComparator;
import net.ddns.mucaho.jnetrobust.util.Timestamp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;
import java.util.TreeSet;

public class Metadata<T> implements Timestamp, Freezable<Metadata<T>> {
    private transient long lastTouched = System.currentTimeMillis();

    private Short staticReference;
    private NavigableSet<Short> dynamicReferences =
            new TreeSet<Short>(SequenceComparator.instance);
    private transient NavigableSet<Short> dynamicReferencesOut =
            CollectionUtils.unmodifiableNavigableSet(dynamicReferences);

    private T value;

    public Metadata(Short staticReference, T value) {
        this.staticReference = staticReference;
        this.value = value;
    }

    public Metadata() {
        super();
    }


    public Short getStaticReference() {
        return staticReference;
    }

    boolean addDynamicReference(Short e) {
        return dynamicReferences.add(e);
    }

    boolean removeDynamicReference(Short e) {
        return dynamicReferences.remove(e);
    }

    void clearDynamicReferences() {
        dynamicReferences.clear();
    }

    public NavigableSet<Short> getDynamicReferences() {
        return dynamicReferencesOut;
    }

    public Short getFirstDynamicReference() {
        return dynamicReferences.isEmpty() ? null : dynamicReferences.first();
    }

    public Short getLastDynamicReference() {
        return dynamicReferences.isEmpty() ? null : dynamicReferences.last();
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
        out += " ( " + staticReference + " ) ";
        for (Short reference : dynamicReferences)
            out += reference + " ";
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
        out.writeShort(staticReference);
        out.writeShort(dynamicReferences.last());
        out.writeObject(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        staticReference = in.readShort();
        dynamicReferences.add(in.readShort());
        value = (T) in.readObject();
    }

    @Override
    public Metadata<T> clone() {
        Metadata<T> clone = new Metadata<T>();
        clone.value = value;
        clone.staticReference = new Short(staticReference);
        clone.dynamicReferences.addAll(dynamicReferences);
        return clone;
    }
}
