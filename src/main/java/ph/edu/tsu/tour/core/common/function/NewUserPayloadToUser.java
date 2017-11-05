package ph.edu.tsu.tour.core.common.function;

import ph.edu.tsu.tour.core.user.User;

import java.util.function.Function;

public class NewUserPayloadToUser implements Function<User.Payload, User> {

    @Override
    public User apply(User.Payload payload) {
        User user = null;
        if (payload != null) {
            user = User.builder()
                    .username(payload.getUsername())
                    .password(payload.getPassword())
                    .email(payload.getEmail())
                    .build();
        }

        return user;
    }

}
