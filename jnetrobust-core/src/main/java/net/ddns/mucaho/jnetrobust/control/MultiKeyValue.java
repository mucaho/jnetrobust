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

public class MultiKeyValue implements Timestamp, Freezable {
    private transient long lastTouched = System.currentTimeMillis();

    private Short staticReference;
    private NavigableSet<Short> dynamicReferences =
            new TreeSet<Short>(SequenceComparator.instance);
    private transient NavigableSet<Short> dynamicReferencesOut =
            CollectionUtils.unmodifiableNavigableSet(dynamicReferences);

    private Object value;

    public MultiKeyValue(Short staticReference, Object value) {
        this.staticReference = staticReference;
        this.value = value;
    }

    public MultiKeyValue() {
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


    public Object getValue() {
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
        out += ": " + this.value.toString();

        return out;
    }

    /**
     * Externalize the multiKeyValue.
     * Static method that does the same thing as {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} .
     *
     * @param multiKeyValue the instance to write
     * @param out           the {@link java.io.ObjectOutput} to write to
     * @throws IOException if an error occurs
     */
    public static void writeExternalStatic(MultiKeyValue multiKeyValue, ObjectOutput out) throws IOException {
        multiKeyValue.writeExternal(out);
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
    public static MultiKeyValue readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        MultiKeyValue multiKeyValue = new MultiKeyValue();
        multiKeyValue.readExternal(in);
        return multiKeyValue;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(staticReference);
        out.writeShort(dynamicReferences.last());
        out.writeObject(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        staticReference = in.readShort();
        dynamicReferences.add(in.readShort());
        value = in.readObject();
    }

    @Override
    public Object clone() {
        MultiKeyValue clone = new MultiKeyValue();
        clone.value = value;
        clone.staticReference = new Short(staticReference);
        clone.dynamicReferences.addAll(dynamicReferences);
        return clone;
    }
}
