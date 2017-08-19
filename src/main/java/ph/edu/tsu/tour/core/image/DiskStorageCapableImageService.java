package ph.edu.tsu.tour.core.image;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface DiskStorageCapableImageService extends ImageService {

    /**
     * @param image the image to write
     * @return saved image
     */
    Image save(RawImage image);

    CompletableFuture<Image> saveAsync(RawImage image);

}