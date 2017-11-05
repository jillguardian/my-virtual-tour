package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;
import java.util.Observer;

public class VerificationTokenDeletingListener implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenCreatingListener.class);
    private final VerificationTokenService verificationTokenService;

    public VerificationTokenDeletingListener(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof UserModifiedEvent) {
            UserModifiedEvent event = (UserModifiedEvent) arg;
            if (event.getAction() == EntityAction.DELETED) {
                VerificationToken verificationToken = verificationTokenService.findByUser(event.getEntity());
                if (verificationToken != null) {
                    verificationTokenService.deleteById(verificationToken.getId());
                }
            }
        }
    }

}
