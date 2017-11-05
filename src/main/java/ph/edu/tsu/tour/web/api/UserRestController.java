package ph.edu.tsu.tour.web.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.common.function.NewUserPayloadToUser;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.core.user.VerificationTokenService;
import ph.edu.tsu.tour.exception.ResourceConflictException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;

import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping(Urls.REST_USER)
class UserRestController {

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final Function<User.Payload, User> newUserPayloadToUser;

    private final VerificationTokenService verificationTokenService;

    @Autowired
    UserRestController(UserService userService,
                       VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
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
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (!payload.getUsername().equals(user.getUsername())) {
            throw new UnsupportedOperationException("Username changes are not allowed");
        }
        if (!payload.getEmail().equals(user.getEmail())) {
            throw new UnsupportedOperationException("Changing of email is not allowed");
        }

        user.setPassword(payload.getPassword());
        User saved = userService.save(user);
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "/reverify", method = RequestMethod.GET)
    public ResponseEntity<?> reverify(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User with email [" + email + "] does not exist");
        }

        if (user.isActivated()) {
            throw new IllegalStateException("User with email [" + email + "] is already activated");
        }

        verificationTokenService.produce(user);
        return ResponseEntity.accepted().build();
    }

}
