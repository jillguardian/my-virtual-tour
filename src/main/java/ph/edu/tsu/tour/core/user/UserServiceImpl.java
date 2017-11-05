package ph.edu.tsu.tour.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(EntityManager entityManager,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            if (userRepository.findByUsername(user.getUsername()) != null) {
                throw new IllegalArgumentException("Username [" + user.getUsername() + "] already exists");
            }
            if (userRepository.findByEmail(user.getEmail()) != null) {
                throw new IllegalArgumentException("Email address [" + user.getEmail() + "] is unavailable for use");
            }
        }
        // FIXME: Causes double encryption.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Iterable<User> findAll() {
        Iterable<User> found = userRepository.findAll();
        for (User user : found) {
            entityManager.detach(user);
        }
        return found;
    }

    @Override
    public User findById(long id) {
        User found = userRepository.findOne(id);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public User findByUsername(String username) {
        User found = userRepository.findByUsername(username);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public User findByEmail(String email) {
        User found = userRepository.findByEmail(email);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public boolean deleteById(long id) {
        userRepository.delete(id);
        boolean exists = userRepository.exists(id);
        if (exists) {
            logger.error("Unable to delete user with id [" + id + "]");
        }
        return !exists;
    }
}
