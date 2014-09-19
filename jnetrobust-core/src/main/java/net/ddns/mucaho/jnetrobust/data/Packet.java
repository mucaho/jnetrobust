package net.ddns.mucaho.jnetrobust.data;

import net.ddns.mucaho.jnetrobust.util.Freezable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class Packet implements Freezable {
    public Packet() {
        super();
    }

    private MultiKeyValue data;
    private short ack;
    private int lastAcks;


    public MultiKeyValue getData() {
        return data;
    }

    public void setData(MultiKeyValue data) {
        this.data = data;
    }

    public short getAck() {
        return ack;
    }

    public void setAck(short ack) {
        this.ack = ack;
    }

    public int getLastAcks() {
        return lastAcks;
    }

    public void setLastAcks(int lastAcks) {
        this.lastAcks = lastAcks;
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    public String toDebugString() {
        return "ReliableUDPPackage:" + "\t"
                + "data = " + data + "\t"
                + "ack = " + ack + "\t"
                + "lastAcks = " + String.format("%33s", Long.toBinaryString(lastAcks)) + "\n";
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
        MultiKeyValue.writeExternalStatic(data, out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ack = in.readShort();
        lastAcks = in.readInt();
        data = MultiKeyValue.readExternalStatic(in);
    }

    @Override
    public Object clone() {
        Packet clone = new Packet();
        clone.ack = ack;
        clone.lastAcks = lastAcks;
        clone.data = (MultiKeyValue) data.clone();
        return clone;
    }
}
