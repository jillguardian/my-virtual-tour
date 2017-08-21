package ph.edu.tsu.tour.core.storage;

/**
 * A storage service capable of acquiring the stream for the stored item.
 * @param <ID> data type of id
 * @param <STREAM> data type of stream
 */
public interface StreamingStorageService<ID, STREAM> extends StorageService<ID> {

    STREAM getStream(ID id) throws Exception;

}
