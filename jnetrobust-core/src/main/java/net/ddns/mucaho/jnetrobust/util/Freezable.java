package net.ddns.mucaho.jnetrobust.util;

import java.io.Externalizable;
import java.io.Serializable;

public interface Freezable extends Serializable, Externalizable, Cloneable {
	public Object clone();
}
