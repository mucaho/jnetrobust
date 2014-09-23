package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.FastLog;

import static net.ddns.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class PendingMapControl<T> extends MapControl<T> {
    public interface TransmissionSuccessListener<T> {
        public void handleAckedData(short dataId, T ackedData);
        public void handleNotAckedData(short dataId, T unackedData);
    }

    private final TransmissionSuccessListener<T> listener;

    public PendingMapControl(TransmissionSuccessListener<T> listener, int maxEntries, int maxEntryOffset,
                             int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
    }


    public void addToPending(short seqNo, Metadata<T> metadata) {
        // discard old entries in pending map
        super.discardEntries();

        // add to pending map
        dataMap.put(seqNo, metadata);
    }


    public void removeFromPending(short ackNo, int lastAcks) {
        // remove multiple (oldest until newest) from pending map
        removeOnBits(ackNo, lastAcks);

        // remove newest from pending map
        notifyAcked(dataMap.removeAll(ackNo), true);
    }

    private void removeOnBits(short localSeq, int lastLocalSeqs) {
        short lastLocalSeq;
        int msbIndex;
        while (lastLocalSeqs != 0) {
            msbIndex = FastLog.log2(lastLocalSeqs);
            lastLocalSeq = (short) (localSeq - msbIndex - OFFSET);
            notifyAcked(dataMap.removeAll(lastLocalSeq), false);
            lastLocalSeqs &= ~(0x1 << msbIndex);
        }

    }


    @Override
    protected void discardEntry(short key) {
        notifyNotAcked(dataMap.removeAll(key));
    }

    protected void notifyNotAcked(Metadata<T> unackedMetadata) {
        if (unackedMetadata != null)
            listener.handleNotAckedData(unackedMetadata.getStaticReference(), unackedMetadata.getData());
    }

    protected void notifyAcked(Metadata<T> ackedMetadata, boolean directlyAcked) {
        if (ackedMetadata != null)
            listener.handleAckedData(ackedMetadata.getStaticReference(), ackedMetadata.getData());
    }
}
