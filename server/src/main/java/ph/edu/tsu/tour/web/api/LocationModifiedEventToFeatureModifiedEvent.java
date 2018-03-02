package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import ph.edu.tsu.tour.web.common.function.LocationToFeature;
import ph.edu.tsu.tour.core.location.LocationModifiedEvent;

import java.util.function.Function;

class LocationModifiedEventToFeatureModifiedEvent
        implements Function<LocationModifiedEvent, FeatureModifiedEvent> {

    private LocationToFeature locationToFeature;

    LocationModifiedEventToFeatureModifiedEvent() {
        this.locationToFeature = new LocationToFeature();
    }

    @Override
    public FeatureModifiedEvent apply(LocationModifiedEvent event) {
        Feature feature = locationToFeature.apply(event.getEntity());
        return new FeatureModifiedEvent(event.getAction(), feature);
    }

}
