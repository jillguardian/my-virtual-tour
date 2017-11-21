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
import ph.edu.tsu.tour.core.common.function.UserPayloadToUser;
import ph.edu.tsu.tour.core.user.NewPasswordToken;
import ph.edu.tsu.tour.core.user.NewPasswordTokenService;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.core.user.VerificationToken;
import ph.edu.tsu.tour.core.user.VerificationTokenService;
import ph.edu.tsu.tour.exception.ResourceConflictException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.Urls;
import ph.edu.tsu.tour.web.common.dto.ChangePasswordPayload;
import ph.edu.tsu.tour.web.common.dto.UserPayload;

import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping(Urls.REST_USER)
class UserRestController {

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final Function<UserPayload, User> newUserPayloadToUser;

    private final VerificationTokenService verificationTokenService;
    private final NewPasswordTokenService newPasswordTokenService;

    @Autowired
    UserRestController(UserService userService,
                       VerificationTokenService verificationTokenService,
                       NewPasswordTokenService newPasswordTokenService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.newPasswordTokenService = newPasswordTokenService;
        newUserPayloadToUser = new UserPayloadToUser();
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public ResponseEntity<User> save(@RequestBody UserPayload payload) {
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
    public ResponseEntity<User> update(Authentication authentication, @RequestBody UserPayload payload) {
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

    @RequestMapping(value = "/reverify", method = RequestMethod.POST)
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

    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public ResponseEntity<?> verify(@RequestParam String token) {
        VerificationToken verificationToken = verificationTokenService.findByContent(token);
        if (verificationToken == null) {
            throw new ResourceNotFoundException("Token [" + token + "] is invalid");
        }

        User user = verificationToken.getUser();
        boolean activated = user.isActivated();
        if (!activated) {
            verificationTokenService.consume(verificationToken);
        } else {
            throw new IllegalStateException(
                    "User [" + verificationToken.getUser().getUsername() + "] is already activated");
        }

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "request-password-reset", method = RequestMethod.POST)
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User with email [" + email + "] does not exist");
        }

        newPasswordTokenService.produce(user);
        logger.info("Created a password reset token for user [" + user.getUsername() + "]");

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "reset-password", method = RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@RequestBody ChangePasswordPayload payload) {
        NewPasswordToken newPasswordToken = newPasswordTokenService.findByContent(payload.getNewPasswordToken());
        if (newPasswordToken == null) {
            throw new ResourceNotFoundException("Token [" + payload.getNewPasswordToken() + "] is invalid");
        }

        User user = newPasswordToken.getUser();
        user.setPassword(payload.getNewPassword());
        userService.save(user);
        logger.info("User [" + user.getUsername() + "]'s password has been successfully reset");

        newPasswordTokenService.consume(newPasswordToken);
        return ResponseEntity.accepted().build();
    }

}
