package ph.edu.tsu.tour.core.map;

import com.mapbox.services.api.directionsmatrix.v1.MapboxDirectionsMatrix;
import com.mapbox.services.api.directionsmatrix.v1.models.DirectionsMatrixResponse;
import com.mapbox.services.commons.models.Position;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.Project;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

        // Keep items in order.
        destinations = new LinkedHashSet<>(destinations);
        List<GeoJsonObject> copy = new ArrayList<>(destinations);

        List<Position> convertedDestinations = destinations.stream()
                .map(Point.class::cast)
                .map(pointToPosition)
                .collect(Collectors.toList());
        List<Position> coordinates = new ArrayList<>();
        coordinates.add(pointToPosition.apply(Point.class.cast(source)));
        coordinates.addAll(convertedDestinations);

        // Get the indexes of destination elements in the coordinates collection.
        int[] indexDestinations = convertedDestinations.stream()
                .map(coordinates::indexOf)
                .mapToInt(coordinate -> coordinate)
                .toArray();
        MapboxDirectionsMatrix.Builder builder = new MapboxDirectionsMatrix.Builder<>()
                .setClientAppName(applicationName)
                .setAccessToken(accessToken)
                .setProfile(profileToProfile.apply(profile))
                .setCoordinates(coordinates)
                .setSources(0)
                .setDestinations(indexDestinations);

        try {
            MapboxDirectionsMatrix client = builder.build();
            Response<DirectionsMatrixResponse> response = client.executeCall();
            if (response.isSuccessful()) {
                double[] durations = response.body().getDurations()[0];
                if (durations != null && durations.length != 0) {
                    int shortest = 0;
                    for (int i = 0; i < durations.length; i++) {
                        if (durations[i] < durations[shortest]) {
                            shortest = i;
                        }
                    }
                    return copy.get(shortest);
                }
            }
            throw new UnsupportedOperationException("Could not get travel durations");
        } catch (IOException e) {
            throw new UncheckedIOException("Request to get information regarding locations failed", e);
        }
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

}
