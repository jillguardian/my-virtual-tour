package ph.edu.tsu.tour.core.poi;

import ph.edu.tsu.tour.core.image.Image;
import ph.edu.tsu.tour.core.image.ToPublicImageService;

import java.util.Collection;
import java.util.Objects;

public class ToPublicPointOfInterestService implements PointOfInterestTransformingService {

    private ToPublicImageService toPublicImageService;

    public ToPublicPointOfInterestService(ToPublicImageService toPublicImageService) {
        this.toPublicImageService = Objects.requireNonNull(toPublicImageService);
    }

    @Override
    public PointOfInterest apply(PointOfInterest pointOfInterest) {
        if (pointOfInterest != null) {
            if (pointOfInterest.getCoverImage1() != null) {
                Image transformed = toPublicImageService.apply(pointOfInterest.getCoverImage1());
                pointOfInterest.setCoverImage1(transformed);
            }
            if (pointOfInterest.getCoverImage2() != null) {
                Image transformed = toPublicImageService.apply(pointOfInterest.getCoverImage2());
                pointOfInterest.setCoverImage2(transformed);
            }
            Collection<Image> rawImages = pointOfInterest.getImages();
            for (Image image : rawImages) {
                pointOfInterest.removeImage(image);
                Image transformed = toPublicImageService.apply(image);
                pointOfInterest.addImage(transformed);
            }
        }
        return pointOfInterest;
    }

}
