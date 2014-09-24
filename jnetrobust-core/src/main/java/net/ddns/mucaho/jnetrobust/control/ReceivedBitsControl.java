package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.ShiftableBitSet;

import java.util.Collection;
import java.util.Comparator;

import static net.ddns.mucaho.jnetrobust.util.BitConstants.OFFSET;
import static net.ddns.mucaho.jnetrobust.util.BitConstants.SIZE;

public class ReceivedBitsControl {


    /*
     * [remoteTransmissionId-32]-[remoteTransmissionId-31]-...-[remoteTransmissionId-1]
     */
    private ShiftableBitSet receivedRemoteBits = new ShiftableBitSet();
    private final Comparator<Short> comparator;

    public ReceivedBitsControl(Comparator<Short> comparator) {
        this.comparator = comparator;
    }


    public long getReceivedRemoteBits() {
        return this.receivedRemoteBits.get();
    }


    public void addToReceived(Collection<Short> remoteTransmissionIds, final short remoteTransmissionId) {
        int diff;
        for (short id : remoteTransmissionIds) {
            diff = comparator.compare(remoteTransmissionId, id);
            addToReceived(diff);
        }
    }

    protected void addToReceived(int diff) {
        // add to received bitset
        if ((diff > 0) && (diff - OFFSET < SIZE)) { // save late pkg.seq into bitset
            receivedRemoteBits.set(diff - OFFSET, true);
        } else if (diff < 0) { // save new remoteTransmissionId: save old remoteTransmissionId into bitSet, then shift for remainder
            receivedRemoteBits.shiftLeft(OFFSET);
            receivedRemoteBits.setLowestBit(true);
            receivedRemoteBits.shiftLeft(-diff - OFFSET);
        }
    }
}
