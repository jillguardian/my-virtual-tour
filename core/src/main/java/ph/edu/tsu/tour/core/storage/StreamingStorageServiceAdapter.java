package ph.edu.tsu.tour.core.storage;

import java.util.function.Function;

/**
 * @param <ID> data type of the actual {@link StreamingStorageService}'s id
 * @param <STREAM> data type of the actual {@link StreamingStorageService}'s stream
 * @param <PORTED_ID>
 * @param <PORTED_STREAM>
 */
public class StreamingStorageServiceAdapter<ID, STREAM, PORTED_ID, PORTED_STREAM>
        implements StreamingStorageService<PORTED_ID, PORTED_STREAM> {

    private StreamingStorageService<ID, STREAM> streamingStorageService;
    private Function<PORTED_ID, ID> unwrapId;
    private Function<STREAM, PORTED_STREAM> wrapStream;

    public StreamingStorageServiceAdapter(StreamingStorageService<ID, STREAM> streamingStorageService,
                                          Function<PORTED_ID, ID> unwrapId,
                                          Function<STREAM, PORTED_STREAM> wrapStream) {
        this.streamingStorageService = streamingStorageService;
        this.unwrapId = unwrapId;
        this.wrapStream = wrapStream;
    }

    @Override
    public void write(PORTED_ID id, byte[] bytes) throws Exception {
        ID unwrappedId = unwrapId.apply(id);
        streamingStorageService.write(unwrappedId, bytes);
    }

    @Override
    public void delete(PORTED_ID id) throws Exception {
        ID unwrappedId = unwrapId.apply(id);
        streamingStorageService.delete(unwrappedId);
    }

    @Override
    public PORTED_STREAM getStream(PORTED_ID id) throws Exception {
        ID unwrappedId = unwrapId.apply(id);
        STREAM stream = streamingStorageService.getStream(unwrappedId);
        return wrapStream.apply(stream);
    }

}
