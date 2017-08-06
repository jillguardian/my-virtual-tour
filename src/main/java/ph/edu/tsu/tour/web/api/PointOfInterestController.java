package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.domain.Image;
import ph.edu.tsu.tour.domain.PointOfInterest;
import ph.edu.tsu.tour.domain.PointsOfInterest;
import ph.edu.tsu.tour.service.ImageService;
import ph.edu.tsu.tour.service.ImageTransformingService;
import ph.edu.tsu.tour.service.PointOfInterestService;
import ph.edu.tsu.tour.service.impl.ToPublicImageUriService;
import ph.edu.tsu.tour.web.Urls;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin
@RestController("restPointOfInterestController")
@RequestMapping(Urls.REST_POI)
public class PointOfInterestController {

    private PointOfInterestService pointOfInterestService;
    private ImageService imageService;
    private ImageTransformingService imageTransformingService;

    @Autowired
    public PointOfInterestController(PointOfInterestService pointOfInterestService,
                                     ImageService imageService,
                                     ToPublicImageUriService toPublicImageUriService) {
        this.pointOfInterestService = pointOfInterestService;
        this.imageService = imageService;
        this.imageTransformingService = toPublicImageUriService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> findAll() {
        Iterable<PointOfInterest> found = pointOfInterestService.findAll();
        Collection<PointOfInterest> modified = new HashSet<>();
        for (PointOfInterest poi : found) {
            PointOfInterest.Builder clone = PointOfInterest.builder(poi);
            if (poi.getPreviewImage1() != null) {
                Image image = imageService.findById(poi.getPreviewImage1().getId());
                image = imageTransformingService.apply(Image.builder(image).build());
                clone.previewImage1(image);
            }
            if (poi.getPreviewImage2() != null) {
                Image image = imageService.findById(poi.getPreviewImage2().getId());
                image = imageTransformingService.apply(Image.builder(image).build());
                clone.previewImage2(image);
            }

            Collection<Image> images = poi.getImages();
            Set<Image> transformedImages = new HashSet<>();
            for (Image image : images) {
                image = imageTransformingService.apply(Image.builder(image).build());
                transformedImages.add(image);
            }
            clone.images(transformedImages);
            modified.add(clone.build());
        }

        FeatureCollection converted = PointsOfInterest.convert(modified);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Feature> findById(@PathVariable long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi != null) {
            PointOfInterest.Builder clone = PointOfInterest.builder(poi);
            if (poi.getPreviewImage1() != null) {
                Image image = imageService.findById(poi.getPreviewImage1().getId());
                image = imageTransformingService.apply(Image.builder(image).build());
                clone.previewImage1(image);
            }
            if (poi.getPreviewImage2() != null) {
                Image image = imageService.findById(poi.getPreviewImage2().getId());
                image = imageTransformingService.apply(Image.builder(image).build());
                clone.previewImage2(image);
            }

            Collection<Image> images = poi.getImages();
            Set<Image> transformedImages = new HashSet<>();
            for (Image image : images) {
                image = imageTransformingService.apply(Image.builder(image).build());
                transformedImages.add(image);
            }
            clone.images(transformedImages);

            return ResponseEntity.ok(PointsOfInterest.convert(clone.build()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }



}
