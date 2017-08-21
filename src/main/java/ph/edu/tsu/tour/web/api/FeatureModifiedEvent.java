package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.core.EntityModifiedEvent;

class FeatureModifiedEvent extends EntityModifiedEvent<Feature> {

    private FeatureModifiedEvent() {
        // Default constructor for Jackson.
        super(null, null);
    }

    FeatureModifiedEvent(EntityAction action, Feature feature) {
        super(action, feature);
    }

}
