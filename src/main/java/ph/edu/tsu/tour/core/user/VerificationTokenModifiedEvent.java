package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.core.EntityModifiedEvent;

public class VerificationTokenModifiedEvent extends EntityModifiedEvent<VerificationToken> {

    public VerificationTokenModifiedEvent(EntityAction action, VerificationToken entity) {
        super(action, entity);
    }

}
