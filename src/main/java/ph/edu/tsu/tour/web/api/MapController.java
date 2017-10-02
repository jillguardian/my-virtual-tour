package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.common.function.PointOfInterestToFeature;
import ph.edu.tsu.tour.core.map.DomainMapService;
import ph.edu.tsu.tour.core.map.Profile;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Urls.REST_MAP)
class MapController {

    private final DomainMapService domainMapService;
    private final PointOfInterestService pointOfInterestService;

    private final Function<PointOfInterest, Feature> pointToFeature;

    @Autowired
    MapController(DomainMapService domainMapService, PointOfInterestService pointOfInterestService) {
        this.domainMapService = domainMapService;
        this.pointOfInterestService = pointOfInterestService;
        this.pointToFeature = new PointOfInterestToFeature();
    }

    @RequestMapping(value = "/nearest-poi", method = RequestMethod.GET)
    public ResponseEntity<Feature> findNearestPointOfInterest(@RequestParam("profile") Profile profile,
                                                              @RequestParam("source-longitude") double sourceLongitude,
                                                              @RequestParam("source-latitude") double sourceLatitude,
                                                              @RequestParam("destination") long[] ids) {
        List<PointOfInterest> destinations = new ArrayList<>();
        for (long id : ids) {
            PointOfInterest poi = pointOfInterestService.findById(id);
            if (poi == null) {
                throw new ResourceNotFoundException("Point of interest with id [" + id + "] does not exist");
            }
            destinations.add(poi);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        PointOfInterest nearest = domainMapService.findNearest(profile, source, destinations);
        if (nearest != null) {
            Feature feature = pointToFeature.apply(nearest);
            return ResponseEntity.ok(feature);
        }
        return ResponseEntity.status(424).build();
    }

}
