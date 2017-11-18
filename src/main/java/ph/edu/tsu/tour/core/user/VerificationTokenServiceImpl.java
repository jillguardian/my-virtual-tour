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
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final int DEFAULT_VERIFICATION_TOKEN_LIFE_SPAN_IN_DAYS = 1;
    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final int verificationTokenLifeSpanInDays;

    public VerificationTokenServiceImpl(EntityManager entityManager,
                                        UserRepository userRepository,
                                        VerificationTokenRepository verificationTokenRepository) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        verificationTokenLifeSpanInDays = VerificationTokenServiceImpl.DEFAULT_VERIFICATION_TOKEN_LIFE_SPAN_IN_DAYS;
    }


    @Override
    public VerificationToken save(VerificationToken verificationToken) {
        // TODO: Remove; affixed for now for debugging purposes.
        String username = verificationToken.getUser().getUsername();
        logger.info("Created verification token [" + verificationToken.getContent() + "] for user [" + username + "]");

        return verificationTokenRepository.save(verificationToken);
    }

    @Override
    public Iterable<VerificationToken> findAll() {
        Iterable<VerificationToken> found = verificationTokenRepository.findAll();
        for (VerificationToken verificationToken : found) {
            entityManager.detach(verificationToken);
        }
        return found;
    }

    @Override
    public VerificationToken findById(long id) {
        VerificationToken found = verificationTokenRepository.findOne(id);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public VerificationToken findByUser(User user) {
        VerificationToken found = verificationTokenRepository.findByUser(user);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public VerificationToken findByContent(String content) {
        VerificationToken found = verificationTokenRepository.findByContent(content);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public boolean deleteById(long id) {
        verificationTokenRepository.delete(id);
        boolean exists = verificationTokenRepository.exists(id);
        if (exists) {
            logger.error("Unable to delete verification token with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public VerificationToken produce(User user) {
        if (user.isActivated()) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] is already verified");
        }

        VerificationToken existing = verificationTokenRepository.findByUser(user);
        if (existing != null) {
            logger.debug("There's already an existing verification token for user [" + user.getUsername() + "]");
            deleteById(existing.getId());
            logger.debug("Existing verification token deleted");
        }
        VerificationToken verificationToken = new VerificationToken(user, UUID.randomUUID().toString());
        return save(verificationToken);
    }

    @Override
    public void consume(VerificationToken verificationToken) {
        User user = verificationToken.getUser();
        if (user.isActivated()) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] is already verified");
        } else {
            OffsetDateTime expiration = verificationToken.getCreated().plusDays(verificationTokenLifeSpanInDays);
            if (verificationToken.getCreated().isBefore(expiration)) {
                // We have to use the repository instance instead of the service to avoid double-password encryption.
                // Currently implementation of the service encrypts the password before saving it.
                user.setActivated(true);
                userRepository.save(user);
                deleteById(verificationToken.getId());
            } else {
                throw new ExpiredResourceException(
                        "Verification token [" + verificationToken.getContent() + "] has already expired");
            }
        }
    }

}
