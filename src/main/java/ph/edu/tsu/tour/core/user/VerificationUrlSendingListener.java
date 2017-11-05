package ph.edu.tsu.tour.core.user;

import org.codehaus.groovy.tools.shell.util.MessageSource;
import org.springframework.mail.MailSender;
import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.web.Urls;

import java.util.Observable;
import java.util.Observer;

/**
 * <p>A listener that sends the verification url to the specified email of the target user.</p>
 */
public class VerificationUrlSendingListener implements Observer {

    private static final String EMAIL_SUBJECT_CODE = "user.verification";
    private static final String CONFIRMATION_URL = Urls.USER + "/verify";

    private final MessageSource messages;
    private final MailSender mailSender;

    public VerificationUrlSendingListener(MessageSource messages, MailSender mailSender) {
        this.messages = messages;
        this.mailSender = mailSender;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof VerificationTokenModifiedEvent) {
            VerificationTokenModifiedEvent event = (VerificationTokenModifiedEvent) arg;
            if (event.getAction() == EntityAction.CREATED || event.getAction() == EntityAction.MODIFIED) {
                User user = event.getEntity().getUser();
                // TODO: Send verification email.
            }
        }
    }

}
