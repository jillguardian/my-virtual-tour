package ph.edu.tsu.tour.service;

import ph.edu.tsu.tour.domain.Image;

import java.io.InputStream;

public interface DiskStorageCapableImageService extends ImageService {

    /**
     * Save the file to disk, and sets the given {@code image}'s {@link Image#location location} to the location
     * of the file.
     *
     * @param image the image to write
     * @param inputStream holds the actual image file to write to disk
     * @return saved image
     */
    Image save(Image image, InputStream inputStream);

}
