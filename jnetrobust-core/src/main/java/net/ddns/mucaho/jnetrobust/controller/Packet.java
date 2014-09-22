package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.Freezable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;


public class Packet implements Freezable {
    public final static transient int MAX_DATAS_PER_PACKET = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) - 1;

    public Packet() {
        super();
    }

    private Deque<MultiKeyValue> datas = new LinkedList<MultiKeyValue>();
    private transient Deque<MultiKeyValue> datasOut = CollectionUtils.unmodifiableDeque(datas);
    private short ack;
    private int lastAcks;


    public Deque<MultiKeyValue> getDatas() {
        return datasOut;
    }

    public MultiKeyValue getFirstData() {
        return datas.peekFirst();
    }

    public MultiKeyValue getLastData() {
        return datas.peekLast();
    }

    void addLastData(MultiKeyValue data) {
        if (datas.size() >= MAX_DATAS_PER_PACKET)
            throw new IndexOutOfBoundsException("Cannot add more than " + MAX_DATAS_PER_PACKET + " datas to packet.");

        datas.addLast(data);
    }

    MultiKeyValue removeFirstData() {
        return datas.pollFirst();
    }

    public short getAck() {
        return ack;
    }

    void setAck(short ack) {
        this.ack = ack;
    }

    public int getLastAcks() {
        return lastAcks;
    }

    void setLastAcks(int lastAcks) {
        this.lastAcks = lastAcks;
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    public String toDebugString() {
        return "Packet:" + "\t"
                + "ack = " + ack + "\t"
                + "lastAcks = " + String.format("%33s", Long.toBinaryString(lastAcks)) + "\t"
                + "datas = " + Arrays.deepToString(datas.toArray()) + "\n";
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
     * @return a new packet instance, constructed by the data read
     * @throws IOException            if an error occurs
     * @throws ClassNotFoundException if an error occurs.
     */
    public static Packet readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        Packet packet = new Packet();
        packet.readExternal(in);
        return packet;
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(ack);
        out.writeInt(lastAcks);
        out.writeByte(datas.size());
        for (MultiKeyValue data: datas)
            MultiKeyValue.writeExternalStatic(data, out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ack = in.readShort();
        lastAcks = in.readInt();
        int size = in.readUnsignedByte();
        for (int i = 0; i < size; ++i)
            datas.addLast(MultiKeyValue.readExternalStatic(in));
    }

    @Override
    public Object clone() {
        Packet clone = new Packet();
        clone.ack = ack;
        clone.lastAcks = lastAcks;
        for (MultiKeyValue data: datas)
            clone.addLastData((MultiKeyValue) data.clone());
        return clone;
    }
}
