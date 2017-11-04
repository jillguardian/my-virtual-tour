package ph.edu.tsu.tour.core.user;

public interface UserService {

    User save(User user);

    Iterable<User> findAll();

    User findById(long id);

    User findByUsername(String username);

    User findByEmail(String email);

    boolean deleteById(long id);

}
