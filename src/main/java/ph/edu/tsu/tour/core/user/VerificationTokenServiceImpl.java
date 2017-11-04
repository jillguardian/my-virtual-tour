package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.UUID;

@Transactional
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    private VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenServiceImpl(EntityManager entityManager,
                                        VerificationTokenRepository verificationTokenRepository) {
        this.entityManager = entityManager;
        this.verificationTokenRepository = verificationTokenRepository;
    }


    @Override
    public VerificationToken save(VerificationToken verificationToken) {
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
    public VerificationToken create(User user) {
        if (user.isActivated()) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] is already verified");
        }
        return new VerificationToken(user, UUID.randomUUID().toString());
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
}
