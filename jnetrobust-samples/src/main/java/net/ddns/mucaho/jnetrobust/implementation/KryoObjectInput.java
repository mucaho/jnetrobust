package net.ddns.mucaho.jnetrobust.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class KryoObjectInput extends Input implements ObjectInput {

	private final Kryo kryo;
	
	public KryoObjectInput(Kryo kryo) {
		super();
		this.kryo = kryo;
	}

	public KryoObjectInput(byte[] buffer, int offset, int count, Kryo kryo) {
		super(buffer, offset, count);
		this.kryo = kryo;
	}

	public KryoObjectInput(byte[] buffer, Kryo kryo) {
		super(buffer);
		this.kryo = kryo;
	}

	public KryoObjectInput(InputStream inputStream, int bufferSize, Kryo kryo) {
		super(inputStream, bufferSize);
		this.kryo = kryo;
	}

	public KryoObjectInput(InputStream inputStream, Kryo kryo) {
		super(inputStream);
		this.kryo = kryo;
	}

	public KryoObjectInput(int bufferSize, Kryo kryo) {
		super(bufferSize);
		this.kryo = kryo;
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) super.skip((long)n);
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return readByteUnsigned();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return readShortUnsigned();
	}

	@Override
	public String readUTF() throws IOException {
		return super.readString();
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		return kryo.readClassAndObject(this);
	}
	
	
	@Override
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Not implemented.");
	}
	
	@Override
	public void readFully(byte[] b) throws IOException {
		super.readBytes(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		super.readBytes(b, off, len);
	}

}
