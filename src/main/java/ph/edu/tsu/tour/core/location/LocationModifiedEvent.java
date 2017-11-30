package ph.edu.tsu.tour.core.location;

import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.core.EntityModifiedEvent;

public class LocationModifiedEvent extends EntityModifiedEvent<Location> {

    LocationModifiedEvent(EntityAction action, Location location) {
        super(action, location);
    }

}
