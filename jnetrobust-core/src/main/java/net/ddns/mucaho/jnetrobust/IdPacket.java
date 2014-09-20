package net.ddns.mucaho.jnetrobust;

import net.ddns.mucaho.jnetrobust.data.Packet;

public class IdPacket {
    private Packet packet;
    private short dataId;

    public IdPacket() {
    }

    public IdPacket(Packet packet, short dataId) {
        this.packet = packet;
        this.dataId = dataId;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public short getDataId() {
        return dataId;
    }

    public void setDataId(short dataId) {
        this.dataId = dataId;
    }

    public static IdPacket immutablePacket(final IdPacket delegate) {
        return new IdPacket() {
            @Override
            public Packet getPacket() {
                return delegate.getPacket();
            }

            @Override
            public void setPacket(Packet packet) {
                throw new UnsupportedOperationException("Can not change field of immutable data object.");
            }

            @Override
            public short getDataId() {
                return delegate.getDataId();
            }

            @Override
            public void setDataId(short dataId) {
                throw new UnsupportedOperationException("Can not change field of immutable data object.");
            }
        };
    }
}
