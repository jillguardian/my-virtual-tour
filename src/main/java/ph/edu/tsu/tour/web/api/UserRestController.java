package ph.edu.tsu.tour.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.common.function.NewUserPayloadToUser;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.exception.ResourceConflictException;
import ph.edu.tsu.tour.web.Urls;

import java.security.Principal;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping(Urls.REST_USER)
class UserRestController {

    private final UserService userService;
    private final Function<User.Payload, User> newUserPayloadToUser;

    @Autowired
    UserRestController(UserService userService) {
        this.userService = userService;
        newUserPayloadToUser = new NewUserPayloadToUser();
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public ResponseEntity<User> save(@RequestBody User.Payload payload) {
        if (userService.findByUsername(payload.getUsername()) != null) {
            throw new ResourceConflictException(
                    "A user with the username [" + payload.getUsername() + "] already exists");
        }
        if (userService.findByEmail(payload.getEmail()) != null) {
            throw new ResourceConflictException("A user with the email [" + payload.getEmail() + "] already exists");
        }

        User saved = userService.save(newUserPayloadToUser.apply(payload));
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<User> update(Authentication authentication, @RequestBody User.Payload payload) {
        // TODO: Get currently authenticated user and update user's info.
        throw new UnsupportedOperationException();
    }

}
