package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.core.EntityModifiedEvent;

public class NewPasswordTokenModifiedEvent extends EntityModifiedEvent<NewPasswordToken> {

    public NewPasswordTokenModifiedEvent(EntityAction action,
                                            NewPasswordToken entity) {
        super(action, entity);
    }

}
