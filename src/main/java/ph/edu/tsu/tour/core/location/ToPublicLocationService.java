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
    public Location apply(Location location) {
        if (location != null) {
            if (location.getCoverImage1() != null) {
                Image transformed = toPublicImageService.apply(location.getCoverImage1());
                location.setCoverImage1(transformed);
            }
            if (location.getCoverImage2() != null) {
                Image transformed = toPublicImageService.apply(location.getCoverImage2());
                location.setCoverImage2(transformed);
            }
            Collection<Image> rawImages = location.getImages();
            for (Image image : rawImages) {
                location.removeImage(image);
                Image transformed = toPublicImageService.apply(image);
                location.addImage(transformed);
            }
        }
        return location;
    }

}
