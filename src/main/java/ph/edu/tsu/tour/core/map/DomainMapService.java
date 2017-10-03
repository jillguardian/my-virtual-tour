package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.List;
import java.util.Set;

public interface DomainMapService {

    PointOfInterest getNearestDestination(Profile profile, GeoJsonObject source, Set<PointOfInterest> destinations);

    List<PointOfInterest> sortDestinations(Profile profile, GeoJsonObject source, Set<PointOfInterest> destinations);

}
