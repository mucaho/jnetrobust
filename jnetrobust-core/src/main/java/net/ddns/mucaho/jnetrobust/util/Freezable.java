package net.ddns.mucaho.jnetrobust.util;

import java.io.Externalizable;
import java.io.Serializable;

public interface Freezable<T> extends Serializable, Externalizable, Cloneable {
    public T clone();
}
