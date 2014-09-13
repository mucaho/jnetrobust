package net.ddns.mucaho.jnetrobust.implementation;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class KryoObjectOutput extends Output implements ObjectOutput {

	private final Kryo kryo;

	
	public KryoObjectOutput(Kryo kryo) {
		super();
		this.kryo = kryo;
	}

	public KryoObjectOutput(byte[] buffer, int maxBufferSize, Kryo kryo) {
		super(buffer, maxBufferSize);
		this.kryo = kryo;
	}

	public KryoObjectOutput(byte[] buffer, Kryo kryo) {
		super(buffer);
		this.kryo = kryo;
	}

	public KryoObjectOutput(int bufferSize, int maxBufferSize, Kryo kryo) {
		super(bufferSize, maxBufferSize);
		this.kryo = kryo;
	}

	public KryoObjectOutput(int bufferSize, Kryo kryo) {
		super(bufferSize);
		this.kryo = kryo;
	}

	public KryoObjectOutput(OutputStream outputStream, int bufferSize, Kryo kryo) {
		super(outputStream, bufferSize);
		this.kryo = kryo;
	}

	public KryoObjectOutput(OutputStream outputStream, Kryo kryo) {
		super(outputStream);
		this.kryo = kryo;
	}

	@Override
	public void writeChar(int v) throws IOException {
		writeShort(v);
	}
	
	@Override
	@Deprecated
	public void writeBytes(String s) throws IOException {
		byte[] out = new byte[s.length()];
		s.getBytes(0, s.length(), out, 0);
		for (byte b: out) {
			writeByte(b);
		}
	}
	@Override
	public void writeChars(String s) throws IOException {
		super.writeChars(s.toCharArray());
	}
	@Override
	public void writeUTF(String s) throws IOException {
		super.writeString(s);
	}
	@Override
	public void writeObject(Object obj) throws IOException {
		kryo.writeClassAndObject(this, obj);
	}
	

}
