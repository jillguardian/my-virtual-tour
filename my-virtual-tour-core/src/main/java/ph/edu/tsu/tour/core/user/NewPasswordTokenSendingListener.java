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

public class NewPasswordTokenSendingListener implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(NewPasswordTokenSendingListener.class);
    private static final String CODE_SUBJECT = "user.reset-password-email.subject";

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final JavaMailSender mailSender;

    private final ExecutorService executorService;

    public NewPasswordTokenSendingListener(TemplateEngine templateEngine,
                                           MessageSource messageSource,
                                           JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.mailSender = mailSender;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof NewPasswordTokenModifiedEvent) {
            NewPasswordTokenModifiedEvent event = (NewPasswordTokenModifiedEvent) arg;
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

                        String html = templateEngine.process("/user/email/password-reset", context);
                        String subject = messageSource.getMessage(NewPasswordTokenSendingListener.CODE_SUBJECT,
                                                                  null,
                                                                  LocaleContextHolder.getLocale());

                        mimeMessageHelper.setTo(user.getEmail());
                        mimeMessageHelper.setText(html, true);
                        mimeMessageHelper.setSubject(subject);

                        mailSender.send(mimeMessage);
                        logger.info("Sent password reset email to [" + user.getEmail() + "]");
                    } catch (Throwable e) {
                        throw new FailedDependencyException("Unable to send password reset token to user", e);
                    }
                }, executorService).whenComplete((result, error) -> {
                    if (error != null) {
                        logger.error("Couldn't send reset-password token to email", error.getCause());
                    }
                });;
            }
        }
    }

}
