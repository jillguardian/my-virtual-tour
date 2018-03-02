package ph.edu.tsu.tour.core.user;

import org.springframework.data.repository.CrudRepository;

public interface NewPasswordTokenRepository extends CrudRepository<NewPasswordToken, Long> {
    NewPasswordToken findByUser(User user);
    NewPasswordToken findByContent(String content);
}
