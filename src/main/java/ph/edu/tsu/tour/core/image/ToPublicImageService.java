package ph.edu.tsu.tour.core.image;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.PubliclyAccessibleFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

/**
 * Converts the image's URIs to <i>public</i> URIs.
 */
public class ToPublicImageService implements Function<Image, Image> {

    private static final Logger logger = LoggerFactory.getLogger(ToPublicImageService.class);

    private FileSystemManager fileSystemManager;
    private FileSystemOptions fileSystemOptions;

    public ToPublicImageService(FileSystemManager fileSystemManager, FileSystemOptions fileSystemOptions) {
        this.fileSystemManager = Objects.requireNonNull(fileSystemManager, "[fileSystemManager] must be set");
        this.fileSystemOptions = Objects.requireNonNull(fileSystemOptions, "[fileSystemOptions] must be set");
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
            FileObject fileObject = fileSystemManager.resolveFile(privateUri.toASCIIString(), fileSystemOptions);
            if (fileObject instanceof PubliclyAccessibleFileObject) {
                return PubliclyAccessibleFileObject.class.cast(fileObject).getPubliclyAccessibleUri();
            }
        } catch (FileSystemException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to convert private URI to public URI");
            }
        }
        return null;
    }

}
