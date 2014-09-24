package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.FastLog;

import static net.ddns.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class PendingMapControl<T> extends MapControl<T> {
    public interface TransmissionSuccessListener<T> {
        public void handleAckedData(short dataId, T ackedData);
        public void handleUnackedData(short dataId, T unackedData);
    }

    private final TransmissionSuccessListener<T> listener;

    public PendingMapControl(TransmissionSuccessListener<T> listener, int maxEntries, int maxEntryOffset,
                             int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
    }


    public void addToPending(short transmissionId, Metadata<T> metadata) {
        // discard old entries in pending map
        super.discardEntries();

        // add to pending map
        dataMap.put(transmissionId, metadata);
    }


    public void removeFromPending(short transmissionId, int precedingTransmissionIds) {
        // remove multiple (oldest until newest) from pending map
        removeOnBits(transmissionId, precedingTransmissionIds);

        // remove newest from pending map
        notifyAcked(dataMap.removeAll(transmissionId), true);
    }

    private void removeOnBits(short transmissionId, int precedingTransmissionIds) {
        short precedingTransmissionId;
        int msbIndex;
        while (precedingTransmissionIds != 0) {
            msbIndex = FastLog.log2(precedingTransmissionIds);
            precedingTransmissionId = (short) (transmissionId - msbIndex - OFFSET);
            notifyAcked(dataMap.removeAll(precedingTransmissionId), false);
            precedingTransmissionIds &= ~(0x1 << msbIndex);
        }

    }


    @Override
    protected void discardEntry(short key) {
        notifyNotAcked(dataMap.removeAll(key));
    }

    protected void notifyNotAcked(Metadata<T> unackedMetadata) {
        if (unackedMetadata != null)
            listener.handleUnackedData(unackedMetadata.getDataId(), unackedMetadata.getData());
    }

    protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
        if (ackedMetadata != null)
            listener.handleAckedData(ackedMetadata.getDataId(), ackedMetadata.getData());
    }
}
