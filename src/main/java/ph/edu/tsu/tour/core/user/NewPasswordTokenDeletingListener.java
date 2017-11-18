package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;
import java.util.Observer;

public class NewPasswordTokenDeletingListener implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(NewPasswordTokenDeletingListener.class);
    private final NewPasswordTokenService newPasswordTokenService;

    public NewPasswordTokenDeletingListener(NewPasswordTokenService newPasswordTokenService) {
        this.newPasswordTokenService = newPasswordTokenService;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof UserModifiedEvent) {
            UserModifiedEvent event = (UserModifiedEvent) arg;
            if (event.getAction() == EntityAction.DELETED) {
                NewPasswordToken newPasswordToken = newPasswordTokenService.findByUser(event.getEntity());
                if (newPasswordToken != null) {
                    newPasswordTokenService.deleteById(newPasswordToken.getId());
                }
            }
        }
    }

}
