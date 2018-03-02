package ph.edu.tsu.tour.core.user;

import org.springframework.data.repository.CrudRepository;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {
    VerificationToken findByUser(User user);
    VerificationToken findByContent(String content);
}
