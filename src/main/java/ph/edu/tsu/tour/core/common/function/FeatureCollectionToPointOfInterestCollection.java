package ph.edu.tsu.tour.core.common.function;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class FeatureCollectionToPointOfInterestCollection
        implements Function<FeatureCollection, Iterable<PointOfInterest>> {

    private Function<Feature, PointOfInterest> featureToPointOfInterest;

    public FeatureCollectionToPointOfInterestCollection() {
        this.featureToPointOfInterest = new FeatureToPointOfInterest();
    }

    @Override
    public Collection<PointOfInterest> apply(FeatureCollection featureCollection) {
        Collection<PointOfInterest> pointOfInterests = new ArrayList<>();
        for (Feature feature : featureCollection.getFeatures()) {
            pointOfInterests.add(featureToPointOfInterest.apply(feature));
        }
        return pointOfInterests;
    }

}
