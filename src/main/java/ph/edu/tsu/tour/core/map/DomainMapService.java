package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.List;

public interface DomainMapService {

    PointOfInterest findNearest(Profile profile, GeoJsonObject source, List<PointOfInterest> destinations);

}
