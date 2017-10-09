package ph.edu.tsu.tour.core.map;

import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.commons.models.Position;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DecoratedMapboxDomainMapService implements DomainMapService {

    private static final String DEFAULT_APPLICATION_NAME = Project.getName() + "/" + Project.getVersion();

    private final MapService mapService;
    private final DomainMapService domainMapService;

    private final String applicationName;
    private final String accessToken;
    private final Function<Point, Position> pointToPosition;
    private final Function<Profile, String> profileToProfile;

    public DecoratedMapboxDomainMapService(String accessToken) {
        this.applicationName = DecoratedMapboxDomainMapService.DEFAULT_APPLICATION_NAME;
        this.accessToken = accessToken;
        this.pointToPosition = new PointToPosition();
        this.profileToProfile = new ProfileToProfile();
        this.mapService = new MapboxMapService(accessToken);
        this.domainMapService = new DefaultDomainMapService(mapService);
    }

    @Override
    public PointOfInterest getNearestDestination(Profile profile,
                                                 GeoJsonObject source,
                                                 Set<PointOfInterest> destinations) {
        return domainMapService.getNearestDestination(profile, source, destinations);
    }

    @Override
    public List<PointOfInterest> sortDestinations(Profile profile,
                                                  GeoJsonObject source,
                                                  Set<PointOfInterest> destinations) {
        return domainMapService.sortDestinations(profile, source, destinations);
    }

    public DirectionsResponse getDirections(Profile profile,
                                            GeoJsonObject source,
                                            Set<PointOfInterest> destinations) {
        Objects.requireNonNull(profile, "Profile must be specified");
        Objects.requireNonNull(source, "Source must be specified");
        Objects.requireNonNull(destinations, "Destinations must be specified");

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("Destinations collection is empty");
        }

        List<PointOfInterest> sorted = domainMapService.sortDestinations(profile, source, destinations);
        List<Position> coordinates = sorted.stream()
                .map(PointOfInterest::getGeometry)
                .map(Point.class::cast)
                .map(pointToPosition)
                .collect(Collectors.toList());

        Point casted = Point.class.cast(source);
        coordinates.add(0, Position.fromCoordinates(
                casted.getCoordinates().getLongitude(),
                casted.getCoordinates().getLatitude(),
                casted.getCoordinates().getAltitude()));

        MapboxDirections.Builder builder = new MapboxDirections.Builder()
                .setClientAppName(applicationName)
                .setAccessToken(accessToken)
                .setCoordinates(coordinates)
                .setProfile(profileToProfile.apply(profile));
        MapboxDirections client = builder.build();

        try {
            Response<DirectionsResponse> response = client.executeCall();
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to get directions", e);
        }
    }

}
