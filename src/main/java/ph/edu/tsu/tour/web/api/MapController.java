package ph.edu.tsu.tour.web.api;

import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.core.common.function.PointOfInterestCollectionToFeatureCollection;
import ph.edu.tsu.tour.core.common.function.PointOfInterestToFeature;
import ph.edu.tsu.tour.core.map.DecoratedMapboxDomainMapService;
import ph.edu.tsu.tour.core.map.DomainMapService;
import ph.edu.tsu.tour.core.map.Profile;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
@RequestMapping(Urls.REST_MAP)
class MapController {

    private final DecoratedMapboxDomainMapService domainMapService;
    private final PointOfInterestService pointOfInterestService;

    private final Function<PointOfInterest, Feature> pointOfInterestToFeature;
    private final Function<Iterable<PointOfInterest>, FeatureCollection> pointOfInterestCollectionToFeatureCollection;

    @Autowired
    MapController(DecoratedMapboxDomainMapService domainMapService,
                  PointOfInterestService pointOfInterestService) {
        this.domainMapService = domainMapService;
        this.pointOfInterestService = pointOfInterestService;
        this.pointOfInterestToFeature = new PointOfInterestToFeature();
        this.pointOfInterestCollectionToFeatureCollection = new PointOfInterestCollectionToFeatureCollection();
    }

    @RequestMapping(value = "/nearest", method = RequestMethod.GET)
    public ResponseEntity<Feature> getNearestDestination(@RequestParam("profile") Profile profile,
                                                         @RequestParam("source-longitude") double sourceLongitude,
                                                         @RequestParam("source-latitude") double sourceLatitude,
                                                         @RequestParam("destination") long[] ids) {
        Set<PointOfInterest> destinations = new HashSet<>();
        for (long id : ids) {
            PointOfInterest poi = pointOfInterestService.findById(id);
            if (poi == null) {
                throw new ResourceNotFoundException("Point of interest with id [" + id + "] does not exist");
            }
            destinations.add(poi);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        PointOfInterest nearest = domainMapService.getNearestDestination(profile, source, destinations);
        Feature feature = pointOfInterestToFeature.apply(nearest);
        return ResponseEntity.ok(feature);
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> sortDestinations(@RequestParam("profile") Profile profile,
                                                              @RequestParam("source-longitude") double sourceLongitude,
                                                              @RequestParam("source-latitude") double sourceLatitude,
                                                              @RequestParam("destination") long[] ids) {
        Set<PointOfInterest> destinations = new HashSet<>();
        for (long id : ids) {
            PointOfInterest poi = pointOfInterestService.findById(id);
            if (poi == null) {
                throw new ResourceNotFoundException("Point of interest with id [" + id + "] does not exist");
            }
            destinations.add(poi);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        List<PointOfInterest> sorted = domainMapService.sortDestinations(profile, source, destinations);
        FeatureCollection converted = pointOfInterestCollectionToFeatureCollection.apply(sorted);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/directions", method = RequestMethod.GET)
    public ResponseEntity<DirectionsResponse> getDirections(@RequestParam("profile") Profile profile,
                                                            @RequestParam("source-longitude") double sourceLongitude,
                                                            @RequestParam("source-latitude") double sourceLatitude,
                                                            @RequestParam("destination") long[] ids) {
        Set<PointOfInterest> destinations = new HashSet<>();
        for (long id : ids) {
            PointOfInterest poi = pointOfInterestService.findById(id);
            if (poi == null) {
                throw new ResourceNotFoundException("Point of interest with id [" + id + "] does not exist");
            }
            destinations.add(poi);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        DirectionsResponse directions = domainMapService.getDirections(profile, source, destinations);
        return ResponseEntity.ok(directions);
    }

}
