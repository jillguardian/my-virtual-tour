package ph.edu.tsu.tour.core.location;

import ph.edu.tsu.tour.core.image.Image;
import ph.edu.tsu.tour.core.image.ToPublicImageService;

import java.util.Collection;
import java.util.Objects;

public class ToPublicLocationService implements LocationTransformingService {

    private ToPublicImageService toPublicImageService;

    public ToPublicLocationService(ToPublicImageService toPublicImageService) {
        this.toPublicImageService = Objects.requireNonNull(toPublicImageService);
    }

    @Override
    public void accept(Location location) {
        if (location != null) {
            if (location.getCoverImage() != null) {
                Image transformed = toPublicImageService.apply(location.getCoverImage());
                location.setCoverImage(transformed);
            }
            Collection<Image> rawImages = location.getImages();
            for (Image image : rawImages) {
                location.removeImage(image);
                Image transformed = toPublicImageService.apply(image);
                location.addImage(transformed);
            }
        }
    }

}
