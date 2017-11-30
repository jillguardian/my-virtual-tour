package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>A listener that sends the verification url to the specified email of the target user.</p>
 */
public class VerificationTokenSendingListener implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenSendingListener.class);
    private static final String CODE_SUBJECT = "user.verification-email.subject";

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final JavaMailSender mailSender;

    private final ExecutorService executorService;

    public VerificationTokenSendingListener(TemplateEngine templateEngine,
                                            MessageSource messageSource,
                                            JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.mailSender = mailSender;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof VerificationTokenModifiedEvent) {
            VerificationTokenModifiedEvent event = (VerificationTokenModifiedEvent) arg;
            if (event.getAction() == EntityAction.CREATED || event.getAction() == EntityAction.MODIFIED) {
                CompletableFuture.runAsync(() -> {
                    try {
                        User user = event.getEntity().getUser();

                        MimeMessage mimeMessage = mailSender.createMimeMessage();
                        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                                                                                    StandardCharsets.UTF_8.name());

                        Context context = new Context();
                        context.setVariable("user", user);
                        context.setVariable("token", event.getEntity().getContent());

                        String html = templateEngine.process("/user/email/verification", context);
                        String subject = messageSource.getMessage(VerificationTokenSendingListener.CODE_SUBJECT,
                                                                  null,
                                                                  LocaleContextHolder.getLocale());

                        mimeMessageHelper.setTo(user.getEmail());
                        mimeMessageHelper.setText(html, true);
                        mimeMessageHelper.setSubject(subject);

                        mailSender.send(mimeMessage);
                        logger.info("Verification email sent to [" + user.getEmail() + "]");
                    } catch (Throwable e) {
                        throw new FailedDependencyException("Unable to send verification mail to user", e);
                    }
                }, executorService).whenComplete((result, error) -> {
                    if (error != null) {
                        logger.error("Couldn't send verification token to email", error.getCause());
                    }
                });
            }
        }
    }

}
