package net.ddns.mucaho.jnetrobust.control;

public class ReceivedMapControl<T> extends MapControl<T> {
    public interface TransmissionOrderListener<T> {
        public void handleOrderedData(short dataId, T orderedData);
        public void handleUnorderedData(short dataId, T unorderedData);
    }

    private final TransmissionOrderListener<T> listener;
    private short nextDataId;

    public ReceivedMapControl(short dataId, TransmissionOrderListener<T> listener, int maxEntries, int maxEntryOffset,
                              int maxEntryOccurrences, long maxEntryTimeout) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout);
        this.listener = listener;
        this.nextDataId = (short) (dataId + 1);
    }


    @Override
    protected void createMap() {
        dataMap = new MetadataMap<T>(comparator) {
            @Override
            Metadata<T> putStatic(Metadata<T> metadata) {
                if (comparator.compare(metadata.getStaticReference(), nextDataId) >= 0) {
//					System.out.print("P["+ref+"]");
                    return super.putStatic(metadata);
                }
                return null;
            }
        };
    }


    public void addToReceived(Metadata<T> metadata) {
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

    private void notifyUnordered(Metadata<T> unorderedMetadata) {
        if (unorderedMetadata != null)
            listener.handleUnorderedData(unorderedMetadata.getStaticReference(), unorderedMetadata.getData());
    }

    private void notifyOrdered(Metadata<T> orderedMetadata) {
        if (orderedMetadata != null)
            listener.handleOrderedData(orderedMetadata.getStaticReference(), orderedMetadata.getData());

    }
}
