package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.control.MetadataUnit;
import net.ddns.mucaho.jnetrobust.util.CollectionUtils;
import net.ddns.mucaho.jnetrobust.util.Freezable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;


public class ProtocolUnit implements Freezable {
    public final static transient int MAX_DATAS_PER_PACKET = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) - 1;

    public ProtocolUnit() {
        super();
    }

    private Deque<MetadataUnit> metadatas = new LinkedList<MetadataUnit>();
    private transient Deque<MetadataUnit> metadatasOut = CollectionUtils.unmodifiableDeque(metadatas);
    private short ack;
    private int lastAcks;


    public Deque<MetadataUnit> getMetadatas() {
        return metadatasOut;
    }

    public MetadataUnit getFirstMetadata() {
        return metadatas.peekFirst();
    }

    public MetadataUnit getLastMetadata() {
        return metadatas.peekLast();
    }

    void addLastMetadata(MetadataUnit metadata) {
        if (metadatas.size() >= MAX_DATAS_PER_PACKET)
            throw new IndexOutOfBoundsException("Cannot add more than " + MAX_DATAS_PER_PACKET + " metadatas to packet.");

        metadatas.addLast(metadata);
    }

    MetadataUnit removeFirstMetadata() {
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
        return "ProtocolUnit:" + "\t"
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
    public static void writeExternalStatic(ProtocolUnit packet, ObjectOutput out) throws IOException {
        packet.writeExternal(out);
    }

    /**
     * Deexternalize the protocolUnit.
     * Static method that does the same thing as {@link java.io.Externalizable#readExternal(java.io.ObjectInput)} .
     *
     * @param in the {@link java.io.ObjectInput} to read from
     * @return a new protocolUnit instance, constructed by the data read
     * @throws IOException            if an error occurs
     * @throws ClassNotFoundException if an error occurs.
     */
    public static ProtocolUnit readExternalStatic(ObjectInput in) throws IOException, ClassNotFoundException {
        ProtocolUnit packet = new ProtocolUnit();
        packet.readExternal(in);
        return packet;
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(ack);
        out.writeInt(lastAcks);
        out.writeByte(metadatas.size());
        for (MetadataUnit metadata: metadatas)
            MetadataUnit.writeExternalStatic(metadata, out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ack = in.readShort();
        lastAcks = in.readInt();
        int size = in.readUnsignedByte();
        for (int i = 0; i < size; ++i)
            metadatas.addLast(MetadataUnit.readExternalStatic(in));
    }

    @Override
    public Object clone() {
        ProtocolUnit clone = new ProtocolUnit();
        clone.ack = ack;
        clone.lastAcks = lastAcks;
        for (MetadataUnit metadata: metadatas)
            clone.addLastMetadata((MetadataUnit) metadata.clone());
        return clone;
    }
}
