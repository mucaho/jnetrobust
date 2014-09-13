package net.ddns.mucaho.jnetrobust.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.ddns.mucaho.jnetrobust.util.Freezable;




public class Packet implements Freezable {
	public Packet() {
		super();
	}

	private MultiKeyValue data = new MultiKeyValue();
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
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(ack);
		out.writeInt(lastAcks);
		data.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ack = in.readShort();
		lastAcks = in.readInt();
		data.readExternal(in);
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
