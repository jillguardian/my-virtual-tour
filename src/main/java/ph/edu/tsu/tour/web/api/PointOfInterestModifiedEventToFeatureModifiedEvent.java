package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import ph.edu.tsu.tour.core.common.function.PointOfInterestCollectionToFeatureCollection;
import ph.edu.tsu.tour.core.common.function.PointOfInterestToFeature;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PointOfInterestModifiedEvent;

import java.util.function.Function;

class PointOfInterestModifiedEventToFeatureModifiedEvent
        implements Function<PointOfInterestModifiedEvent, FeatureModifiedEvent> {

    private PointOfInterestToFeature pointOfInterestToFeature;

    PointOfInterestModifiedEventToFeatureModifiedEvent() {
        this.pointOfInterestToFeature = new PointOfInterestToFeature();
    }

    @Override
    public FeatureModifiedEvent apply(PointOfInterestModifiedEvent event) {
        Feature feature = pointOfInterestToFeature.apply(event.getEntity());
        return new FeatureModifiedEvent(event.getAction(), feature);
    }

}
