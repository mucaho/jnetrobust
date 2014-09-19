package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.FastLog;

import static net.ddns.mucaho.jnetrobust.util.BitConstants.OFFSET;

public class PendingMapControl extends MapControl {
    public interface TransmissionSuccessListener {
        public void handleAckedTransmission(Object ackedPkg);

        public void handleNotAckedTransmission(Object timedoutPkg);
    }

    private final TransmissionSuccessListener listener;

    public PendingMapControl(TransmissionSuccessListener listener,
                             int maxEntryOffset, long maxEntryTimeout) {
        super(maxEntryOffset, maxEntryTimeout);
        this.listener = listener;
    }


    protected void addToPending(short ref, MultiKeyValue data) {
        // discard old entries in pending map
        super.discardEntries();

        // add to pending map
        dataMap.put(ref, data);
    }


    protected void removeFromPending(short ack, int lastAcks) {
        // remove multiple (oldest until newest) from pending map
        removeOnBits(ack, lastAcks);

        // remove newest from pending map
//		System.out.print("A["+ack+"]");
//		System.out.print(dataMap.get(ack));
        notifyAcked(dataMap.removeAll(ack), true);
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
            listener.handleNotAckedTransmission(timedoutPkg.getValue());
    }

    protected void notifyAcked(MultiKeyValue ackedPkg, boolean directlyAcked) {
        if (ackedPkg != null)
            listener.handleAckedTransmission(ackedPkg.getValue());
    }
}
