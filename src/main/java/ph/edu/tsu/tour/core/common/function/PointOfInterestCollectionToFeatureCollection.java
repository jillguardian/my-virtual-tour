package ph.edu.tsu.tour.core.common.function;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.function.Function;

public class PointOfInterestCollectionToFeatureCollection
        implements Function<Iterable<PointOfInterest>, FeatureCollection> {

    private Function<PointOfInterest, Feature> pointOfInterestToFeature;

    public PointOfInterestCollectionToFeatureCollection() {
        this.pointOfInterestToFeature = new PointOfInterestToFeature();
    }

    @Override
    public FeatureCollection apply(Iterable<PointOfInterest> pointsOfInterest) {
        FeatureCollection features = new FeatureCollection();
        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            features.add(pointOfInterestToFeature.apply(pointOfInterest));
        }
        return features;
    }

}
