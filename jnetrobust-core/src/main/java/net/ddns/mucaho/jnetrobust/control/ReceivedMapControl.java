package net.ddns.mucaho.jnetrobust.control;

public class ReceivedMapControl extends MapControl {
    public interface TransmissionOrderListener {
        public void handleOrderedData(short dataId, Object orderedData);
        public void handleUnorderedData(short dataId, Object unorderedData);
    }

    private final TransmissionOrderListener listener;
    private short nextDataId;

    public ReceivedMapControl(short dataId, TransmissionOrderListener listener, int maxEntries, int maxEntryOffset,
                              int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
        this.nextDataId = (short) (dataId + 1);
    }


    @Override
    protected void createMap() {
        dataMap = new MultiKeyValueMap(comparator) {
            @Override
            MultiKeyValue putStatic(MultiKeyValue data) {
                if (comparator.compare(data.getStaticReference(), nextDataId) >= 0) {
//					System.out.print("P["+ref+"]");
                    return super.putStatic(data);
                }
                return null;
            }
        };
    }


    public void addToReceived(MultiKeyValue data) {
        // discard old entries in received map
        super.discardEntries();

        // add original to received map
        dataMap.putStatic(data);

        // remove multiple from map -> least, consecutive, ordered elements
        removeTail();
    }

    private void removeTail() {
        Short key = dataMap.firstKey();
        while (key != null && key == nextDataId) {
//			System.out.print("R["+key+"]");
            notifyOrdered(dataMap.removeStatic(key));

            key = dataMap.higherKey(key);
            nextDataId++;
        }
    }

    @Override
    protected void discardEntry(short key) {
        nextDataId = comparator.compare((short) (key + 1), nextDataId) > 0 ?
                (short) (key + 1) : nextDataId;
        notifyUnordered(dataMap.removeStatic(key));
    }

    private void notifyUnordered(MultiKeyValue unorderedPkg) {
        if (unorderedPkg != null)
            listener.handleUnorderedData(unorderedPkg.getStaticReference(), unorderedPkg.getValue());
    }

    private void notifyOrdered(MultiKeyValue orderedPackage) {
        if (orderedPackage != null)
            listener.handleOrderedData(orderedPackage.getStaticReference(), orderedPackage.getValue());

    }
}
