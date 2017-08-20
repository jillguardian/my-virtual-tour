package ph.edu.tsu.tour.core.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.storage.StreamingStorageService;

import java.net.URI;

public class ToPublicImageServiceImpl implements ToPublicImageService {

    private static final Logger logger = LoggerFactory.getLogger(ToPublicImageServiceImpl.class);

    private StreamingStorageService<URI, URI> streamingStorageService;

    public ToPublicImageServiceImpl(StreamingStorageService<URI, URI> streamingStorageService) {
        this.streamingStorageService = streamingStorageService;
    }

    @Override
    public Image apply(Image image) {
        if (image != null) {
            URI location = image.getLocation();
            URI preview = image.getPreview();

            URI publicLocation = toPublicUri(location);
            image.setLocation(publicLocation);
            image.setPreview(publicLocation);

            if (!preview.equals(location)) {
                URI publicPreview = toPublicUri(preview);
                image.setPreview(publicPreview);
            }
        }
        return image;
    }

    protected URI toPublicUri(URI privateUri) {
        try {
            return streamingStorageService.getStream(privateUri);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to convert private URI to public URI", e);
            }
        }
        return null;
    }

}
