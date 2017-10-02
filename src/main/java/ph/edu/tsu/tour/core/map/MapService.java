package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;
import org.geojson.Geometry;
import org.geojson.GeometryCollection;

import java.util.Collection;
import java.util.List;

public interface MapService {

    /**
     * @return index of the nearest destination from the {@code destinations} collection
     */
    int findNearest(Profile profile, GeoJsonObject source, List<GeoJsonObject> destinations);

}
