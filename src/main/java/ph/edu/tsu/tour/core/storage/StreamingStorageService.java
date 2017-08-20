package ph.edu.tsu.tour.core.storage;

public interface StreamingStorageService<ID, STREAM> extends StorageService<ID> {

    STREAM getStream(ID id) throws Exception;

}
