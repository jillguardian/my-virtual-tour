package ph.edu.tsu.tour.core.user;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ph.edu.tsu.tour.core.EntityAction;
import ph.edu.tsu.tour.exception.FailedDependencyException;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Observable;
import java.util.Observer;

/**
 * <p>A listener that sends the verification url to the specified email of the target user.</p>
 */
public class VerificationTokenSendingListener implements Observer {

    private static final String CODE_SUBJECT = "user.verification-email.subject";

    private final TemplateEngine templateEngine;
    private final MessageSource messages;
    private final JavaMailSender mailSender;

    public VerificationTokenSendingListener(TemplateEngine templateEngine,
                                            MessageSource messages,
                                            JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.messages = messages;
        this.mailSender = mailSender;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof VerificationTokenModifiedEvent) {
            VerificationTokenModifiedEvent event = (VerificationTokenModifiedEvent) arg;
            if (event.getAction() == EntityAction.CREATED || event.getAction() == EntityAction.MODIFIED) {
                try {
                    User user = event.getEntity().getUser();

                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                                                                                StandardCharsets.UTF_8.name());

                    Context context = new Context();
                    context.setVariable("user", user);
                    context.setVariable("token", event.getEntity().getContent());

                    String html = templateEngine.process("/user/email/verification", context);
                    String subject = messages.getMessage(VerificationTokenSendingListener.CODE_SUBJECT,
                                                         null,
                                                         LocaleContextHolder.getLocale());

                    mimeMessageHelper.setTo(user.getEmail());
                    mimeMessageHelper.setText(html, true);
                    mimeMessageHelper.setSubject(subject);

                    mailSender.send(mimeMessage);
                } catch (Throwable e) {
                    throw new FailedDependencyException("Unable to send verification mail to user", e);
                }
            }
        }
    }

}
