package ph.edu.tsu.tour.core.poi;

import ph.edu.tsu.tour.core.EntityModifiedEvent;
import ph.edu.tsu.tour.core.EntityAction;

public class PointOfInterestModifiedEvent extends EntityModifiedEvent<PointOfInterest> {

    PointOfInterestModifiedEvent(EntityAction action, PointOfInterest pointOfInterest) {
        super(action, pointOfInterest);
    }

}
