package ph.edu.tsu.tour.core.common.function;

import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.web.common.dto.UserPayload;

import java.util.function.Function;

public class UserPayloadToUser implements Function<UserPayload, User> {

    @Override
    public User apply(UserPayload payload) {
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
