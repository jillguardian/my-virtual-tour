package ph.edu.tsu.tour.web.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.commons.models.Position;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.web.common.function.LocationCollectionToFeatureCollection;
import ph.edu.tsu.tour.web.common.function.LocationToFeature;
import ph.edu.tsu.tour.core.map.DecoratedMapboxDomainMapService;
import ph.edu.tsu.tour.core.map.Profile;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.exception.FailedDependencyException;
import ph.edu.tsu.tour.exception.IllegalArgumentException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
@RequestMapping(Urls.REST_V1_MAP)
class MapRestController<T extends Location> {

    private static final String DEFAULT_APPLICATION_NAME = Project.getName() + "/" + Project.getVersion();
    private static final Splitter DEFAULT_SEMICOLON_SPLITTER = Splitter.on(';').trimResults();
    private static final Splitter DEFAULT_COMMA_SPLITTER = Splitter.on(',');

    private final DecoratedMapboxDomainMapService domainMapService;
    private final LocationService<T> locationService;

    private final Function<Location, Feature> locationToFeature;
    private final Function<Iterable<Location>, FeatureCollection> locationCollectionToFeatureCollection;

    private final String applicationName;
    private final String accessToken;

    @Autowired
    MapRestController(DecoratedMapboxDomainMapService domainMapService,
                      LocationService<T> locationService,
                      @Value("${application.map.mapbox.access-token}") String accessToken) {
        this.domainMapService = domainMapService;
        this.locationService = locationService;
        this.locationToFeature = new LocationToFeature();
        this.locationCollectionToFeatureCollection = new LocationCollectionToFeatureCollection();
        this.applicationName = MapRestController.DEFAULT_APPLICATION_NAME;
        this.accessToken = accessToken;
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

    @RequestMapping(value = "/directions/{profile}/{coordinates:.*}", method = RequestMethod.GET)
    public ResponseEntity<DirectionsResponse> getDirections(
            @PathVariable String profile,
            @PathVariable String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) String geometries,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String radiuses,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false, value = "continue_straight") Boolean continueStraight,
            @RequestParam(required = false) String bearings,
            @RequestParam(required = false) String annotations,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, value = "roundabout_exits") Boolean roundaboutExits) {
        List<Position> convertedPositions = new ArrayList<>();
        for (String coordinate : MapRestController.DEFAULT_SEMICOLON_SPLITTER.split(coordinates)) {
            String[] split = coordinate.split(",");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid coordinate [" + coordinate + "]");
            } else {
                double longitude = Double.valueOf(split[0]);
                double latitude = Double.valueOf(split[1]);
                convertedPositions.add(Position.fromCoordinates(longitude, latitude));
            }
        }

        String[] temporaryRadiuses = Iterables.toArray(MapRestController.DEFAULT_SEMICOLON_SPLITTER.split(radiuses),
                                                       String.class);
        double[] convertedRadiuses = new double[temporaryRadiuses.length];
        for (int i = 0; i < temporaryRadiuses.length; i++) {
            String radius = temporaryRadiuses[i];
            try {
                convertedRadiuses[i] = Double.valueOf(radius);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid radius [" + radius + "]");
            }
        }

        String[] temporaryBearings = Iterables.toArray(MapRestController.DEFAULT_SEMICOLON_SPLITTER.split(bearings),
                                                       String.class);
        double[][] convertedBearings = new double[temporaryBearings.length][2];
        for (int i = 0; i < temporaryBearings.length; i++) {
            String bearing = temporaryBearings[i];
            if (bearing.isEmpty()) {
                convertedBearings[i] = new double[0];
                continue;
            }

            try {
                double[] value = Arrays.stream(bearing.split(",")).mapToDouble(Double::valueOf).toArray();
                convertedBearings[i] = value;
            } catch (Exception e) {
                throw new IllegalArgumentException("Found invalid value in bearing [" + bearing + "]");
            }
        }

        String[] convertedAnnotations = null;
        if (annotations != null) {
            convertedAnnotations = Iterables.toArray(MapRestController.DEFAULT_COMMA_SPLITTER.split(annotations),
                                                     String.class);
        }

        MapboxDirections.Builder builder = new MapboxDirections.Builder()
                .setClientAppName(applicationName)
                .setAccessToken(accessToken)
                .setProfile(profile)
                .setCoordinates(convertedPositions)
                .setAlternatives(alternatives)
                .setGeometry(geometries)
                .setOverview(overview)
                .setRadiuses(convertedRadiuses)
                .setSteps(steps)
                .setContinueStraight(continueStraight)
                .setBearings(convertedBearings)
                .setAnnotation(convertedAnnotations)
                .setLanguage(language)
                .setRoundaboutExits(roundaboutExits);
        MapboxDirections client = builder.build();
        try {
            Response<DirectionsResponse> response = client.executeCall();
            return ResponseEntity
                    .status(response.code())
                    .body(response.body());
        } catch (IOException e) {
            throw new FailedDependencyException("Unable to get directions", e);
        }
    }

}