package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.FastLog;

import static net.ddns.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class PendingMapControl extends MapControl {
    public interface TransmissionSuccessListener {
        public void handleAckedTransmission(short dataId, Object ackedData);

        public void handleNotAckedTransmission(short dataId, Object unackedData);
    }

    private final TransmissionSuccessListener listener;

    public PendingMapControl(TransmissionSuccessListener listener, int maxEntries, int maxEntryOffset,
                             int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
    }


    public void addToPending(short seqNo, MultiKeyValue data) {
        // discard old entries in pending map
        super.discardEntries();

        // add to pending map
        dataMap.put(seqNo, data);
    }


    public void removeFromPending(short ackNo, int lastAcks) {
        // remove multiple (oldest until newest) from pending map
        removeOnBits(ackNo, lastAcks);

        // remove newest from pending map
//		System.out.print("A["+ack+"]");
//		System.out.print(dataMap.get(ack));
        notifyAcked(dataMap.removeAll(ackNo), true);
    }

    private void removeOnBits(short localSeq, int lastLocalSeqs) {
        short lastLocalSeq;
        int msbIndex;
        while (lastLocalSeqs != 0) {
            msbIndex = FastLog.log2(lastLocalSeqs);
            lastLocalSeq = (short) (localSeq - msbIndex - OFFSET);
//			System.out.print("A["+lastLocalSeq+"]");
//			System.out.print(dataMap.get(lastLocalSeq));
            notifyAcked(dataMap.removeAll(lastLocalSeq), false);
            lastLocalSeqs &= ~(0x1 << msbIndex);
        }

    }


    @Override
    protected void discardEntry(short key) {
        notifyNotAcked(dataMap.removeAll(key));
    }

    protected void notifyNotAcked(MultiKeyValue timedoutPkg) {
        if (timedoutPkg != null)
            listener.handleNotAckedTransmission(timedoutPkg.getStaticReference(), timedoutPkg.getValue());
    }

    protected void notifyAcked(MultiKeyValue ackedPkg, boolean directlyAcked) {
        if (ackedPkg != null)
            listener.handleAckedTransmission(ackedPkg.getStaticReference(), ackedPkg.getValue());
    }
}
