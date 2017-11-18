package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.exception.ExpiredResourceException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public class NewPasswordTokenServiceImpl implements NewPasswordTokenService {

    private static final int DEFAULT_NEW_TOKEN_LIFE_SPAN_IN_DAYS = 1;
    private static final Logger logger = LoggerFactory.getLogger(NewPasswordTokenServiceImpl.class);

    @PersistenceContext
    private final EntityManager entityManager;
    private final NewPasswordTokenRepository newPasswordTokenRepository;

    private final int newPasswordTokenLifeSpanInDays;

    public NewPasswordTokenServiceImpl(EntityManager entityManager,
                                       NewPasswordTokenRepository newPasswordTokenRepository) {
        this.entityManager = entityManager;
        this.newPasswordTokenRepository = newPasswordTokenRepository;
        this.newPasswordTokenLifeSpanInDays = NewPasswordTokenServiceImpl.DEFAULT_NEW_TOKEN_LIFE_SPAN_IN_DAYS;
    }


    @Override
    public NewPasswordToken save(NewPasswordToken newPasswordToken) {
        // TODO: Remove; affixed for now for debugging purposes.
        String username = newPasswordToken.getUser().getUsername();
        logger.info("Created new password token [" + newPasswordToken.getContent() + "] for user [" + username + "]");

        return newPasswordTokenRepository.save(newPasswordToken);
    }

    @Override
    public Iterable<NewPasswordToken> findAll() {
        Iterable<NewPasswordToken> found = newPasswordTokenRepository.findAll();
        for (NewPasswordToken newPasswordToken : found) {
            entityManager.detach(newPasswordToken);
        }
        return found;
    }

    @Override
    public NewPasswordToken findById(long id) {
        NewPasswordToken found = newPasswordTokenRepository.findOne(id);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public NewPasswordToken findByUser(User user) {
        NewPasswordToken found = newPasswordTokenRepository.findByUser(user);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public NewPasswordToken findByContent(String content) {
        NewPasswordToken found = newPasswordTokenRepository.findByContent(content);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public boolean deleteById(long id) {
        newPasswordTokenRepository.delete(id);
        boolean exists = newPasswordTokenRepository.exists(id);
        if (exists) {
            logger.error("Unable to delete verification token with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public NewPasswordToken produce(User user) {
        NewPasswordToken existing = newPasswordTokenRepository.findByUser(user);
        if (existing != null) {
            logger.debug("There's already an existing new password token for user [" + user.getUsername() + "]");
            deleteById(existing.getId());
            logger.debug("Existing new password token deleted");
        }
        NewPasswordToken newPasswordToken = new NewPasswordToken(user, UUID.randomUUID().toString());
        return save(newPasswordToken);
    }

    @Override
    public void consume(NewPasswordToken newPasswordToken) {
        OffsetDateTime expiration = newPasswordToken.getCreated().plusDays(newPasswordTokenLifeSpanInDays);
        if (newPasswordToken.getCreated().isBefore(expiration)) {
            deleteById(newPasswordToken.getId());
        } else {
            throw new ExpiredResourceException(
                    "Password reset token [" + newPasswordToken.getContent() + "] has already expired");
        }
    }

}
