package ph.edu.tsu.tour.core.user;

public interface VerificationTokenService {

    VerificationToken save(VerificationToken verificationToken);

    Iterable<VerificationToken> findAll();

    VerificationToken findById(long id);

    VerificationToken findByUser(User user);

    VerificationToken findByContent(String content);

    VerificationToken create(User user);

    boolean deleteById(long id);

}
