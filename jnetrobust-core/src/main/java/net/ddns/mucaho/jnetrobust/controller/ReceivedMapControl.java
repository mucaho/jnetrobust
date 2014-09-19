package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValueMap;

public class ReceivedMapControl extends MapControl {
    public interface TransmissionOrderListener {
        public void handleOrderedTransmission(Object orderedPkg);

        public void handleUnorderedTransmission(Object unorderedPkg);
    }

    private final TransmissionOrderListener listener;
    private short nextRemoteSeq;

    public ReceivedMapControl(short remoteSeq, TransmissionOrderListener listener,
                              int maxEntryOffset, long maxEntryTimeout) {
        super(maxEntryOffset, maxEntryTimeout);
        this.listener = listener;
        this.nextRemoteSeq = (short) (remoteSeq + 1);
    }


    @Override
    protected void createMap() {
        dataMap = new MultiKeyValueMap(comparator) {
            @Override
            public void put(Short ref, MultiKeyValue data) {
                if (comparator.compare(ref, nextRemoteSeq) >= 0) {
//					System.out.print("P["+ref+"]");
                    super.put(ref, data);
                }
            }
        };
    }


    protected void addToReceived(MultiKeyValue data) {
        // discard old entries in received map
        super.discardEntries();

        // add original to received map
        dataMap.put(data.getStaticReference(), data);

        // remove multiple from map -> least, consecutive, ordered elements
        removeTail();
    }

    private void removeTail() {
        Short key = dataMap.firstKey();
        while (key != null && key == nextRemoteSeq) {
//			System.out.print("R["+key+"]");
            notifyOrdered(dataMap.remove(key));

            key = dataMap.higherKey(key);
            nextRemoteSeq++;
        }
    }

    @Override
    protected void discardEntry(short key) {
        nextRemoteSeq = comparator.compare((short) (key + 1), nextRemoteSeq) > 0 ?
                (short) (key + 1) : nextRemoteSeq;
        notifyUnordered(dataMap.remove(key));
    }

    private void notifyUnordered(MultiKeyValue unorderedPkg) {
        if (unorderedPkg != null)
            listener.handleUnorderedTransmission(unorderedPkg.getValue());
    }

    private void notifyOrdered(MultiKeyValue orderedPackage) {
        if (orderedPackage != null)
            listener.handleOrderedTransmission(orderedPackage.getValue());

    }
}
