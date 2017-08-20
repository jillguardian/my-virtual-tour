package ph.edu.tsu.tour.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DelegatingStreamingStorageService implements StreamingStorageService<URI, URI> {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingStreamingStorageService.class);
    
    private Map<String, StreamingStorageService<URI, URI>> schemeToStreamingStorageService;
    private URI base;

    public DelegatingStreamingStorageService(
            Map<String, StreamingStorageService<URI, URI>> schemeToStreamingStorageService) {
        this(schemeToStreamingStorageService, null);
    }

    public DelegatingStreamingStorageService(
            Map<String, StreamingStorageService<URI, URI>> schemeToStreamingStorageService, URI base) {
        this.schemeToStreamingStorageService = Objects.requireNonNull(schemeToStreamingStorageService);
        this.base = base;
    }
    
    protected Optional<StreamingStorageService<URI, URI>> resolveStorageService(URI uri) {
        if (uri.isAbsolute()) {
            String scheme = uri.getScheme();
            StreamingStorageService<URI, URI> storageService = schemeToStreamingStorageService.get(scheme);
            return Optional.ofNullable(storageService);
        }
        if (base != null) {
            logger.trace("Resolving [" + uri + "] to [" + base + "]...");
            uri = base.resolve(uri);
            return resolveStorageService(uri);
        }
        
        return Optional.empty();
    }

    protected URI resolveWrappedUri(URI uri) {
        String path = uri.getPath();
        return URI.create(path);
    }

    @Override
    public URI getStream(URI uri) throws Exception {
        Optional<StreamingStorageService<URI, URI>> storageService = resolveStorageService(uri);
        if (storageService.isPresent()) {
            return storageService.get().getStream(resolveWrappedUri(uri));
        } else {
            throw new UnsupportedOperationException("Could not find storage service for [" + uri + "]");
        }
    }

    @Override
    public void write(URI uri, byte[] bytes) throws Exception {
        Optional<StreamingStorageService<URI, URI>> storageService = resolveStorageService(uri);
        if (storageService.isPresent()) {
            storageService.get().write(resolveWrappedUri(uri), bytes);
        } else {
            throw new UnsupportedOperationException("Could not find storage service for [" + uri + "]");
        }
    }

    @Override
    public void delete(URI uri) throws Exception {
        Optional<StreamingStorageService<URI, URI>> storageService = resolveStorageService(uri);
        if (storageService.isPresent()) {
            storageService.get().delete(resolveWrappedUri(uri));
        } else {
            throw new UnsupportedOperationException("Could not find storage service for [" + uri + "]");
        }
    }

}
