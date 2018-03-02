package ph.edu.tsu.tour.core.storage;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VfsBasedDelegatingStreamingStorageService extends DelegatingStreamingStorageService<URI, URI> {

    private Map<String, StreamingStorageService<URI, URI>> schemeToStreamingStorageService;
    private URI baseDirectory;

    public VfsBasedDelegatingStreamingStorageService(
            Map<String, StreamingStorageService<URI, URI>> schemeToStreamingStorageService, URI baseDirectory) {
        this.schemeToStreamingStorageService = new HashMap<>(schemeToStreamingStorageService);
        this.baseDirectory = baseDirectory;
    }

    @Override
    protected Optional<StreamingStorageService<URI, URI>> resolveStreamingStorageService(URI uri) {
        if (baseDirectory != null) {
            uri = baseDirectory.resolve(uri);
        }

        String scheme = uri.getScheme();
        return Optional.ofNullable(schemeToStreamingStorageService.get(scheme));
    }

    @Override
    public URI getStream(URI uri) throws Exception {
        if (baseDirectory != null) {
            uri = baseDirectory.resolve(uri);
        }
        return super.getStream(uri);
    }

    @Override
    public void write(URI uri, byte[] bytes) throws Exception {
        if (baseDirectory != null) {
            uri = baseDirectory.resolve(uri);
        }
        super.write(uri, bytes);
    }

    @Override
    public void delete(URI uri) throws Exception {
        if (baseDirectory != null) {
            uri = baseDirectory.resolve(uri);
        }
        super.delete(uri);
    }
}
