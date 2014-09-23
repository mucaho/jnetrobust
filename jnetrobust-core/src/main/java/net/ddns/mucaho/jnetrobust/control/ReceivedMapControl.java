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
        dataMap = new MetadataMap(comparator) {
            @Override
            Metadata putStatic(Metadata metadata) {
                if (comparator.compare(metadata.getStaticReference(), nextDataId) >= 0) {
//					System.out.print("P["+ref+"]");
                    return super.putStatic(metadata);
                }
                return null;
            }
        };
    }


    public void addToReceived(Metadata metadata) {
        // discard old entries in received map
        super.discardEntries();

        // add original to received map
        dataMap.putStatic(metadata);

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

    private void notifyUnordered(Metadata unorderedMetadata) {
        if (unorderedMetadata != null)
            listener.handleUnorderedData(unorderedMetadata.getStaticReference(), unorderedMetadata.getData());
    }

    private void notifyOrdered(Metadata orderedMetadata) {
        if (orderedMetadata != null)
            listener.handleOrderedData(orderedMetadata.getStaticReference(), orderedMetadata.getData());

    }
}
