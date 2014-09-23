package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.control.Metadata;
import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.Freezable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;


public class Packet<T> implements Freezable<Packet<T>> {
    public final static transient int MAX_DATAS_PER_PACKET = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) - 1;

    public Packet() {
        super();
    }

    private Deque<Metadata<T>> metadatas = new LinkedList<Metadata<T>>();
    private transient Deque<Metadata<T>> metadatasOut = CollectionUtils.unmodifiableDeque(metadatas);
    private short ack;
    private int lastAcks;


    public Deque<Metadata<T>> getMetadatas() {
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


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(ack);
        out.writeInt(lastAcks);
        out.writeByte(metadatas.size());
        for (Metadata<T> metadata: metadatas)
            Metadata.<T>writeExternalStatic(metadata, out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ack = in.readShort();
        lastAcks = in.readInt();
        int size = in.readUnsignedByte();
        for (int i = 0; i < size; ++i)
            metadatas.addLast(Metadata.<T>readExternalStatic(in));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Packet<T> clone() {
        Packet<T> clone = new Packet<T>();
        clone.ack = ack;
        clone.lastAcks = lastAcks;
        for (Metadata<T> metadata: metadatas)
            clone.addLastMetadata(metadata.clone());
        return clone;
    }
}
