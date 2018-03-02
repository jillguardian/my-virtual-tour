package ph.edu.tsu.tour.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class DelegatingStreamingStorageService<ID, STREAM> implements StreamingStorageService<ID, STREAM> {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingStreamingStorageService.class);

    protected abstract Optional<StreamingStorageService<ID, STREAM>> resolveStreamingStorageService(ID id);

    @Override
    public STREAM getStream(ID id) throws Exception {
        return resolveStreamingStorageService(id)
                .orElseThrow(() ->
                        new UnsupportedOperationException("Could not find steaming storage service for [" + id + "]"))
                .getStream(id);
    }

    @Override
    public void write(ID id, byte[] bytes) throws Exception {
        resolveStreamingStorageService(id)
                .orElseThrow(() -> new UnsupportedOperationException("Could not find storage service for [" + id + "]"))
                .write(id, bytes);
    }

    @Override
    public void delete(ID id) throws Exception {
        resolveStreamingStorageService(id)
                .orElseThrow(() -> new UnsupportedOperationException("Could not find storage service for [" + id + "]"))
                .delete(id);
    }

}
