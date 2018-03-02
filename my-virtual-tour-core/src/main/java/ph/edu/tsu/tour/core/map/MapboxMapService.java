package ph.edu.tsu.tour.core.map;

import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsWaypoint;
import com.mapbox.services.commons.models.Position;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.exception.FailedDependencyException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapboxMapService implements MapService {

    private static final String DEFAULT_APPLICATION_NAME = Project.getName() + "/" + Project.getVersion();
    private static final Logger logger = LoggerFactory.getLogger(MapboxMapService.class);

    private final String applicationName;
    private final String accessToken;
    private final Function<Point, Position> pointToPosition;
    private final Function<Profile, String> profileToProfile;

    public MapboxMapService(String accessToken) {
        this.applicationName = MapboxMapService.DEFAULT_APPLICATION_NAME;
        this.accessToken = accessToken;
        this.pointToPosition = new PointToPosition();
        this.profileToProfile = new ProfileToProfile();
    }

    @Override
    public GeoJsonObject getNearestDestination(Profile profile, GeoJsonObject source, Set<GeoJsonObject> destinations) {
        Objects.requireNonNull(profile, "Profile must be specified");
        Objects.requireNonNull(source, "Source must be specified");
        Objects.requireNonNull(destinations, "Destinations must be specified");

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("Destinations collection is empty");
        }
        if (!(source instanceof Point) || !destinations.stream().allMatch(geometry -> geometry instanceof Point)) {
            throw new UnsupportedOperationException("Implementation supports only point-based locations");
        }

        Position convertedSource = pointToPosition.apply(Point.class.cast(source));
        List<Position> convertedDestinations = destinations.stream()
                .map(Point.class::cast)
                .map(pointToPosition)
                .collect(Collectors.toList());

        Map<GeoJsonObject, MapboxDirections> destinationToRequestMap = new HashMap<>();
        for (GeoJsonObject destination : destinations) {
            Position converted = pointToPosition.apply(Point.class.cast(destination));
            MapboxDirections request = new MapboxDirections.Builder()
                    .setAccessToken(accessToken)
                    .setClientAppName(applicationName)
                    .setProfile(profileToProfile.apply(profile))
                    .setOrigin(convertedSource)
                    .setDestination(converted)
                    .build();
            destinationToRequestMap.put(destination, request);
        }

        Map<GeoJsonObject, DirectionsResponse> destinationToResponseMap = new HashMap<>();
        BiConsumer<GeoJsonObject, DirectionsResponse> responseBiConsumer = destinationToResponseMap::put;

        CountDownLatch latch = new CountDownLatch(destinationToRequestMap.size());
        for (GeoJsonObject destination : destinationToRequestMap.keySet()) {
            MapboxDirections request = destinationToRequestMap.get(destination);
            request.enqueueCall(new MapboxDirectionsCallback(destination, responseBiConsumer, latch));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Unable to properly execute requests", e);
        }

        GeoJsonObject nearest = destinationToResponseMap.keySet().stream()
                .min((o1, o2) -> {
                    DirectionsResponse o1Response = destinationToResponseMap.get(o1);
                    DirectionsResponse o2Response = destinationToResponseMap.get(o2);
                    if (o1Response.getRoutes().size() != 1 || o2Response.getRoutes().size() != 1) {
                        throw new AssertionError("Found more than one route");
                    }
                    return Double.compare(o1Response.getRoutes().get(0).getDistance(),
                                          o2Response.getRoutes().get(0).getDistance());
                }).orElseThrow(() -> new FailedDependencyException("Couldn't find shortest route via supporting API"));

       return nearest;
    }

    @Override
    public List<GeoJsonObject> sortDestinations(Profile profile,
                                                GeoJsonObject source,
                                                Set<GeoJsonObject> destinations) {
        Objects.requireNonNull(profile, "Profile must be specified");
        Objects.requireNonNull(source, "Source must be specified");
        Objects.requireNonNull(destinations, "Destinations must be specified");

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("Destinations collection is empty");
        }

        Set<GeoJsonObject> original = new HashSet<>(destinations);
        List<GeoJsonObject> sorted = new ArrayList<>();
        while (!original.isEmpty()) {
            GeoJsonObject nearest = getNearestDestination(profile, source, original);
            original.remove(nearest);
            sorted.add(nearest);
            source = nearest;
        }

        return sorted;
    }

    private static class MapboxDirectionsCallback implements Callback<DirectionsResponse> {

        private final GeoJsonObject destination;
        private final BiConsumer<GeoJsonObject, DirectionsResponse> consumer;
        private final CountDownLatch latch;

        private MapboxDirectionsCallback(GeoJsonObject destination,
                                         BiConsumer<GeoJsonObject, DirectionsResponse> consumer,
                                         CountDownLatch latch) {
            this.destination = destination;
            this.latch = latch;
            this.consumer = consumer;
        }

        @Override
        public void onResponse(Call<DirectionsResponse> call,
                               Response<DirectionsResponse> response) {
            consumer.accept(destination, response.body());
            latch.countDown();
        }

        @Override
        public void onFailure(Call<DirectionsResponse> call,
                              Throwable throwable) {
            latch.countDown();
            throw new IllegalArgumentException("Unable to get directions", throwable);
        }
    }

}
