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
    private short transmissionAck;
    private int precedingTransmissionAcks;


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

    public short getTransmissionAck() {
        return transmissionAck;
    }

    void setTransmissionAck(short ack) {
        this.transmissionAck = ack;
    }

    public int getPrecedingTransmissionAcks() {
        return precedingTransmissionAcks;
    }

    void setPrecedingTransmissionAcks(int lastAcks) {
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


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(transmissionAck);
        out.writeInt(precedingTransmissionAcks);
        out.writeByte(metadatas.size());
        for (Metadata<T> metadata: metadatas)
            Metadata.<T>writeExternalStatic(metadata, out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        transmissionAck = in.readShort();
        precedingTransmissionAcks = in.readInt();
        int size = in.readUnsignedByte();
        for (int i = 0; i < size; ++i)
            metadatas.addLast(Metadata.<T>readExternalStatic(in));
    }

    @Override
    public Packet<T> clone() {
        Packet<T> clone = new Packet<T>();
        clone.transmissionAck = transmissionAck;
        clone.precedingTransmissionAcks = precedingTransmissionAcks;
        for (Metadata<T> metadata: metadatas)
            clone.addLastMetadata(metadata.clone());
        return clone;
    }
}
