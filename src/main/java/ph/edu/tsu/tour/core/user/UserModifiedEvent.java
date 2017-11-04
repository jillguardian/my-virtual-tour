package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.core.EntityModifiedEvent;

public class UserModifiedEvent extends EntityModifiedEvent<User> {

    UserModifiedEvent(EntityAction action, User entity) {
        super(action, entity);
    }

}
