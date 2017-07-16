package ph.edu.tsu.tour.repository;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByUsername(String username);
}
