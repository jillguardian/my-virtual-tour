package ph.edu.tsu.tour.core.map;

import org.geojson.GeoJsonObject;

import java.util.List;
import java.util.Set;

public interface MapService {

    /**
     * <p>Given a starting location {@code source}, finds the nearest destination from the {@code destinations}
     * collection.</p>
     *
     * @param profile the preferred profile to use when travelling to destination
     * @param source starting location
     * @param destinations possible locations to choose from
     * @return the nearest destination from the {@code destinations} collection
     */
    GeoJsonObject getNearestDestination(Profile profile, GeoJsonObject source, Set<GeoJsonObject> destinations);

    /**
     * <p>Given a {@code destinations} collection, sorts said collection to optimal traveling order, given that the
     * starting point is at {@code source}.</p>
     * <p>You may refer to the traveling salesman problem.</p>
     *
     * @param profile the preferred profile to use when travelling to destinations
     * @param source starting location
     * @param destinations locations required to visit
     * @return sorted {@code destinations}
     */
    List<GeoJsonObject> sortDestinations(Profile profile, GeoJsonObject source, Set<GeoJsonObject> destinations);

}
