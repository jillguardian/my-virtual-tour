package ph.edu.tsu.tour.service.impl;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.PubliclyAccessibleFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.domain.Image;
import ph.edu.tsu.tour.service.ImageTransformingService;

import java.net.URI;
import java.util.Objects;

public class ToPublicImageUriService implements ImageTransformingService {

    private static final Logger logger = LoggerFactory.getLogger(ToPublicImageUriService.class);

    private FileSystemManager fileSystemManager;
    private FileSystemOptions fileSystemOptions;

    public ToPublicImageUriService(FileSystemManager fileSystemManager, FileSystemOptions fileSystemOptions) {
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
