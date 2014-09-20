package net.ddns.mucaho.jnetrobust.data;

public class Data {
    private Object data;
    private short dataId;

    public Data() {
    }

    public Data(Object data, short dataId) {
        this.data = data;
        this.dataId = dataId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public short getDataId() {
        return dataId;
    }

    public void setDataId(short dataId) {
        this.dataId = dataId;
    }

    public static Data immutableData(final Data delegate) {
        return new Data() {
            @Override
            public Object getData() {
                return delegate.getData();
            }

            @Override
            public void setData(Object data) {
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
