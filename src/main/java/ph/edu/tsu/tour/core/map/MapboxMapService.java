package ph.edu.tsu.tour.core.map;

import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directionsmatrix.v1.MapboxDirectionsMatrix;
import com.mapbox.services.api.directionsmatrix.v1.models.DirectionsMatrixResponse;
import com.mapbox.services.commons.models.Position;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapboxMapService implements MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapboxMapService.class);

    private final String applicationName;
    private final String accessToken;
    private final Function<Point, Position> pointToPosition;
    private final Function<Profile, String> profileToProfile;

    public MapboxMapService(String applicationName, String accessToken) {
        this.applicationName = applicationName;
        this.accessToken = accessToken;
        this.pointToPosition = new PointToPosition();
        this.profileToProfile = new ProfileToProfile();
    }

    @Override
    public int findNearest(Profile profile, GeoJsonObject source, List<GeoJsonObject> destinations) {
        if (!(source instanceof Point) || !destinations.stream().allMatch(geometry -> geometry instanceof Point)) {
            logger.debug("Implementation supports only point-based locations");
            return -1;
        }

        List<Position> convertedDestinations = destinations.stream()
                .map(Point.class::cast)
                .map(pointToPosition)
                .collect(Collectors.toList());
        List<Position> coordinates = new ArrayList<>();
        coordinates.add(pointToPosition.apply(Point.class.cast(source)));
        coordinates.addAll(convertedDestinations);

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
                if (durations != null) {
                    int shortest = 0;
                    for (int i = 0; i < durations.length; i++) {
                        if (durations[i] < durations[shortest]) {
                            shortest = i;
                        }
                    }
                    return shortest;
                }
            }
            logger.debug("Could not get duration information");
            return -1;
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not get information regarding locations", e);
            }
            return -1;
        }
    }

    private static class PointToPosition implements Function<Point, Position> {
        @Override
        public Position apply(Point point) {
            if (point != null) {
                return Position.fromCoordinates(
                        point.getCoordinates().getLongitude(),
                        point.getCoordinates().getLatitude(),
                        point.getCoordinates().getAltitude());
            }
            return null;
        }
    }

    private static class ProfileToProfile implements Function<Profile, String> {
        @Override
        public String apply(Profile profile) {
            switch (profile) {
                case CYCLING: return DirectionsCriteria.PROFILE_CYCLING;
                case DRIVING: return DirectionsCriteria.PROFILE_DRIVING;
                case WALKING: return DirectionsCriteria.PROFILE_WALKING;
            }
            throw new IllegalArgumentException("Unknown profile [" + profile + "]");
        }
    }

}
