package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultDomainMapService implements DomainMapService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDomainMapService.class);
    private final MapService mapService;

    public DefaultDomainMapService(MapService mapService) {
        this.mapService = mapService;
    }

    @Override
    public PointOfInterest getNearestDestination(Profile profile,
                                                 GeoJsonObject source,
                                                 Set<PointOfInterest> destinations) {
        Objects.requireNonNull(profile, "Profile must be specified");
        Objects.requireNonNull(source, "Source must be specified");
        Objects.requireNonNull(destinations, "Destinations must be specified");

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("Destinations collection is empty");
        }

        Map<GeoJsonObject, PointOfInterest> locationToDomain = new HashMap<>();
        for (PointOfInterest destination : destinations) {
            locationToDomain.put(destination.getGeometry(), destination);
        }

        GeoJsonObject nearest = mapService.getNearestDestination(profile, source, locationToDomain.keySet());
        return locationToDomain.get(nearest);
    }

    @Override
    public List<PointOfInterest> sortDestinations(Profile profile,
                                                  GeoJsonObject source,
                                                  Set<PointOfInterest> destinations) {
        Objects.requireNonNull(profile, "Profile must be specified");
        Objects.requireNonNull(source, "Source must be specified");
        Objects.requireNonNull(destinations, "Destinations must be specified");

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("Destinations collection is empty");
        }

        Map<GeoJsonObject, PointOfInterest> locationToDomain = new HashMap<>();
        for (PointOfInterest destination : destinations) {
            locationToDomain.put(destination.getGeometry(), destination);
        }

        Set<GeoJsonObject> keys = locationToDomain.keySet();
        List<GeoJsonObject> intermediates = mapService.sortDestinations(profile, source, keys);

        List<PointOfInterest> sorted = new ArrayList<>();
        for (GeoJsonObject intermediate : intermediates) {
            sorted.add(locationToDomain.get(intermediate));
        }
        return sorted;
    }

}
