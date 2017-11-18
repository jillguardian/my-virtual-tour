package ph.edu.tsu.tour.core.user;

public interface NewPasswordTokenService {

    NewPasswordToken save(NewPasswordToken newPasswordToken);

    Iterable<NewPasswordToken> findAll();

    NewPasswordToken findById(long id);

    NewPasswordToken findByUser(User user);

    NewPasswordToken findByContent(String content);

    boolean deleteById(long id);

    /**
     * <p>Produces a {@code VerificationToken} for the given user.</p>
     * @param user the user to generate the token for
     * @return generated token
     */
    NewPasswordToken produce(User user);

    void consume(NewPasswordToken newPasswordToken);

}
