package ph.edu.tsu.tour.core.user;

public interface VerificationTokenService {

    VerificationToken save(VerificationToken verificationToken);

    Iterable<VerificationToken> findAll();

    VerificationToken findById(long id);

    VerificationToken findByUser(User user);

    VerificationToken findByContent(String content);

    boolean deleteById(long id);

    /**
     * <p>Produces a {@code VerificationToken} for the given user.</p>
     * @param user the user to generate the token for
     * @return generated token
     */
    VerificationToken produce(User user);

    /**
     * <p>Use the {@code verificationToken} to verify the user.</p>
     * @param verificationToken the token to use
     */
    void consume(VerificationToken verificationToken);

}
