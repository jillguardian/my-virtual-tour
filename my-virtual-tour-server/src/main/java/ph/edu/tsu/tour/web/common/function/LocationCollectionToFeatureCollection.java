package ph.edu.tsu.tour.web.common.function;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.location.Location;

import java.util.function.Function;

public class LocationCollectionToFeatureCollection
        implements Function<Iterable<Location>, FeatureCollection> {

    private Function<Location, Feature> locationToFeature;

    public LocationCollectionToFeatureCollection() {
        this.locationToFeature = new LocationToFeature();
    }

    @Override
    public FeatureCollection apply(Iterable<Location> locations) {
        FeatureCollection features = new FeatureCollection();
        for (Location location : locations) {
            features.add(locationToFeature.apply(location));
        }
        return features;
    }

}
