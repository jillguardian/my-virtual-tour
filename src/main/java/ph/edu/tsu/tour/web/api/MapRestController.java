package ph.edu.tsu.tour.web.api;

import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.common.function.LocationCollectionToFeatureCollection;
import ph.edu.tsu.tour.core.common.function.LocationToFeature;
import ph.edu.tsu.tour.core.map.DecoratedMapboxDomainMapService;
import ph.edu.tsu.tour.core.map.Profile;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
@RequestMapping(Urls.REST_V1_MAP)
class MapRestController {

    private final DecoratedMapboxDomainMapService domainMapService;
    private final LocationService locationService;

    private final Function<Location, Feature> locationToFeature;
    private final Function<Iterable<Location>, FeatureCollection> locationCollectionToFeatureCollection;

    @Autowired
    MapRestController(DecoratedMapboxDomainMapService domainMapService,
                      LocationService locationService) {
        this.domainMapService = domainMapService;
        this.locationService = locationService;
        this.locationToFeature = new LocationToFeature();
        this.locationCollectionToFeatureCollection = new LocationCollectionToFeatureCollection();
    }

    @RequestMapping(value = "/nearest", method = RequestMethod.GET)
    public ResponseEntity<Feature> getNearestDestination(@RequestParam("profile") Profile profile,
                                                         @RequestParam("source-longitude") double sourceLongitude,
                                                         @RequestParam("source-latitude") double sourceLatitude,
                                                         @RequestParam("destination") long[] ids) {
        Set<Location> destinations = new HashSet<>();
        for (long id : ids) {
            Location location = locationService.findById(id);
            if (location == null) {
                throw new ResourceNotFoundException("Location with id [" + id + "] does not exist");
            }
            destinations.add(location);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        Location nearest = domainMapService.getNearestDestination(profile, source, destinations);
        Feature feature = locationToFeature.apply(nearest);
        return ResponseEntity.ok(feature);
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> sortDestinations(@RequestParam("profile") Profile profile,
                                                              @RequestParam("source-longitude") double sourceLongitude,
                                                              @RequestParam("source-latitude") double sourceLatitude,
                                                              @RequestParam("destination") long[] ids) {
        Set<Location> destinations = new HashSet<>();
        for (long id : ids) {
            Location location = locationService.findById(id);
            if (location == null) {
                throw new ResourceNotFoundException("Location with id [" + id + "] does not exist");
            }
            destinations.add(location);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        List<Location> sorted = domainMapService.sortDestinations(profile, source, destinations);
        FeatureCollection converted = locationCollectionToFeatureCollection.apply(sorted);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/directions", method = RequestMethod.GET)
    public ResponseEntity<DirectionsResponse> getDirections(@RequestParam("profile") Profile profile,
                                                            @RequestParam("source-longitude") double sourceLongitude,
                                                            @RequestParam("source-latitude") double sourceLatitude,
                                                            @RequestParam("destination") long[] ids) {
        Set<Location> destinations = new HashSet<>();
        for (long id : ids) {
            Location location = locationService.findById(id);
            if (location == null) {
                throw new ResourceNotFoundException("Location with id [" + id + "] does not exist");
            }
            destinations.add(location);
        }

        Point source = new Point(sourceLongitude, sourceLatitude);
        DirectionsResponse directions = domainMapService.getDirections(profile, source, destinations);
        return ResponseEntity.ok(directions);
    }

}
