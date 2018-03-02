package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;
import ph.edu.tsu.tour.core.location.Location;

import java.util.List;
import java.util.Set;

public interface DomainMapService {

    Location getNearestDestination(Profile profile, GeoJsonObject source, Set<Location> destinations);

    List<Location> sortDestinations(Profile profile, GeoJsonObject source, Set<Location> destinations);

}
