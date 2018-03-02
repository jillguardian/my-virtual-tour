package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * <p>A listener that produces and persists a verification token for newly-added users.</p>
 */
public class VerificationTokenCreatingListener implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenCreatingListener.class);
    private final VerificationTokenService verificationTokenService;

    public VerificationTokenCreatingListener(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof UserModifiedEvent) {
            UserModifiedEvent event = (UserModifiedEvent) arg;
            if (event.getAction() == EntityAction.CREATED) {
                VerificationToken token = verificationTokenService.produce(event.getEntity());
            }
        }
    }

}
