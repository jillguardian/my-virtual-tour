package ph.edu.tsu.tour.web.common.function;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.location.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class FeatureCollectionToLocationCollection
        implements Function<FeatureCollection, Iterable<Location>> {

    private Function<Feature, Location> featureToLocation;

    public FeatureCollectionToLocationCollection() {
        this.featureToLocation = new FeatureToLocation();
    }

    @Override
    public Collection<Location> apply(FeatureCollection featureCollection) {
        Collection<Location> locations = new ArrayList<>();
        for (Feature feature : featureCollection.getFeatures()) {
            locations.add(featureToLocation.apply(feature));
        }
        return locations;
    }

}
