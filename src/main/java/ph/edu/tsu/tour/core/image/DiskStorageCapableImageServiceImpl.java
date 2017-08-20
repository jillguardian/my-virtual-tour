package ph.edu.tsu.tour.core.image;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.storage.StorageService;
import ph.edu.tsu.tour.exception.ImageFileUnsavedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class DiskStorageCapableImageServiceImpl implements DiskStorageCapableImageService {

    private static final Logger logger = LoggerFactory.getLogger(DiskStorageCapableImageServiceImpl.class);

    private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();
    private static final String DEFAULT_IMAGE_DIRECTORY = "Images";

    private ImageSettings mainSettings;
    private ImageSettings previewSettings;
    private StorageService<URI> storageService;
    private Supplier<URI> placeholderUri = () ->
            URI.create("http://via.placeholder.com/350x250?text=" + LocalDateTime.now());
    private Supplier<URI> destinationUri;

    private Executor executor;
    private ImageService imageService;

    public DiskStorageCapableImageServiceImpl(ImageService imageService,
                                              StorageService<URI> storageService,
                                              ImageSettings mainSettings,
                                              ImageSettings previewSettings) {
        this.executor = Executors.newCachedThreadPool();
        this.imageService = Objects.requireNonNull(imageService, "[imageService] must be set");
        this.storageService = Objects.requireNonNull(storageService, "[storageService] must be set");
        this.mainSettings = Objects.requireNonNull(mainSettings, "[mainSettings] must be set");
        this.previewSettings = Objects.requireNonNull(previewSettings, "[previewSettings] must be set");
        this.destinationUri = () -> URI.create("/" + DEFAULT_IMAGE_DIRECTORY + "/");
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public Image findById(long id) {
        return imageService.findById(id);
    }

    @Override
    public Iterable<Image> findAll() {
        return imageService.findAll();
    }

    @Override
    public Image save(Image image) {
        return imageService.save(image);
    }

    @Override
    public boolean deleteById(long id) {
        Image image = findById(id);
        if (image != null) {
            URI mainLocation = image.getLocation();
            URI previewLocation = image.getPreview();
            try {
                storageService.delete(mainLocation);
                if (!mainLocation.equals(previewLocation)) {
                    storageService.delete(previewLocation);
                }
            } catch (Exception e) {
                logger.error("Failed to delete image from storage", e);
                return false;
            }
        }

        return imageService.deleteById(id);
    }

    @Override
    public boolean exists(long id) {
        return imageService.exists(id);
    }

    @Override
    public Image save(RawImage rawImage) {
        InputStream inputStream = rawImage.getInputStream();
        Path path;
        try {
            path = Files.createTempFile("IMG_", null);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to write stream to temporary file", e);
            }
            throw new ImageFileUnsavedException("Could not save image file", e);
        }

        MediaType mediaType;
        try {
            inputStream = new FileInputStream(path.toFile());
            mediaType = DiskStorageCapableImageServiceImpl.getMediaType(inputStream);
            inputStream.close();
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to get media type", e);
            }
            throw new ImageFileUnsavedException("Could not save image file", e);
        }

        // Let's check if it's really an image.
        if (!DiskStorageCapableImageServiceImpl.isImage(mediaType)) {
            throw new IllegalArgumentException("Stream is not an image");
        }

        // Let's guess the extension.
        String extension;
        try {
            extension = DiskStorageCapableImageServiceImpl.getExtension(mediaType);
        } catch (MimeTypeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to determine file extension", e);
            }
            throw new ImageFileUnsavedException("Could not save image file", e);
        }

        try {
            String basename = com.google.common.io.Files.getNameWithoutExtension(path.toString());
            Path original = path;
            path = original.getParent().resolve(basename + extension);

            Files.move(original, path);
            Files.deleteIfExists(original);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to rename temporary file", e);
            }
        }

        Image image = Image.builder()
                .id(rawImage.getId())
                .title(rawImage.getTitle())
                .description(rawImage.getDescription())
                .build();
        if (rawImage.getId() != null && exists(rawImage.getId())) {
            Image found = findById(rawImage.getId());
            image.setLocation(found.getLocation());
            image.setPreview(found.getPreview());
        } else {
            URI placeholder = placeholderUri.get();
            image.setLocation(placeholder);
            image.setPreview(placeholder);
        }
        imageService.save(image);

        // And then generate the final URI.
        URI destination;
        try {
            destination = destinationUri.get().resolve(image.getId() + extension);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to generate destination URI");
            }
            throw new ImageFileUnsavedException("Could not save image file", e);
        }

        // Reconfigure image.
        Path temporary = DiskStorageCapableImageServiceImpl.configure(path.toFile(), mainSettings).toPath();

        try {
            storageService.write(destination, Files.readAllBytes(temporary));
            image.setLocation(destination);
            image.setPreview(destination);
            logger.trace("Successfully wrote image to [" + destination + "]");
            if (!temporary.equals(path)) {
                boolean deleted = temporary.toFile().delete();
                if (!deleted) {
                    logger.debug("Failed to delete [" + temporary + "]");
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to write image to storage", e);
            }
            throw new ImageFileUnsavedException("Could not save image file", e);
        }

        temporary = DiskStorageCapableImageServiceImpl.configure(path.toFile(), previewSettings).toPath();
        try {
            if (!temporary.equals(path)) {
                URI previewDestination = destinationUri.get().resolve(image.getId() + "_PREVIEW" + extension);
                storageService.write(previewDestination, Files.readAllBytes(temporary));
                image.setPreview(previewDestination);
                logger.trace("Successfully wrote preview of image to [" + previewDestination + "]");

                boolean deleted = temporary.toFile().delete();
                if (!deleted) {
                    logger.debug("Failed to delete [" + temporary + "]");
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to write preview image to storage", e);
            }
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.debug("Unable to delete [" + path + "]");
        }
        return imageService.save(image);
    }

    @Override
    public CompletableFuture<Image> saveAsync(RawImage image) {
        return CompletableFuture.supplyAsync( () -> save(image), executor );
    }

    private static File configure(File original, ImageSettings imageSettings) {
        if (imageSettings.getQuality() != 1 || imageSettings.isAutoResize()) {
            try {
                String extension = com.google.common.io.Files.getFileExtension(original.getName());
                File file = Files.createTempFile("IMG_MODIFIED_", "." + extension).toFile();
                Thumbnails.Builder<File> builder = Thumbnails.of(original);
                builder.outputQuality(imageSettings.getQuality());
                if (imageSettings.isAutoResize()) {
                    builder.size(imageSettings.getMaxWidth(), imageSettings.getMaxHeight()).keepAspectRatio(true);
                }

                builder.toFile(file);
                return file;
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to re-configure image", e);
                }
            }
        }

        logger.trace("Returning original file...");
        return original;
    }

    private static MediaType getMediaType(InputStream inputStream) throws IOException {
        inputStream = TikaInputStream.get(inputStream);
        MimeTypes mimeTypes = DiskStorageCapableImageServiceImpl.TIKA_CONFIG.getMimeRepository();
        return mimeTypes.detect(inputStream, new Metadata());
    }

    private static boolean isImage(MediaType mediaType) {
        String type = mediaType.getType();
        return type.equals("image");
    }

    private static String getExtension(MediaType mediaType) throws MimeTypeException {
        MimeType mimeType = TIKA_CONFIG.getMimeRepository().forName(mediaType.toString());
        return mimeType.getExtension();
    }

    @Getter
    @EqualsAndHashCode
    @Builder(builderClassName = "Builder", toBuilder = true)
    public static final class ImageSettings {

        private boolean autoResize;
        private int maxWidth;
        private int maxHeight;
        private double quality;

    }

}
