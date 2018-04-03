package ph.edu.tsu.tour.web.common.function;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.location.Location;

import java.util.function.Function;

public class LocationCollectionToFeatureCollection<T extends Location>
        implements Function<Iterable<T>, FeatureCollection> {

    private Function<Location, Feature> locationToFeature;

    public LocationCollectionToFeatureCollection() {
        this.locationToFeature = new LocationToFeature();
    }

    @Override
    public FeatureCollection apply(Iterable<T> locations) {
        FeatureCollection features = new FeatureCollection();
        for (Location location : locations) {
            features.add(locationToFeature.apply(location));
        }
        return features;
    }

}
