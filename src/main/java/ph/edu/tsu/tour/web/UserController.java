package ph.edu.tsu.tour.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ph.edu.tsu.tour.core.user.PublishingUserService;
import ph.edu.tsu.tour.core.user.PublishingVerificationTokenService;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.core.user.VerificationToken;
import ph.edu.tsu.tour.core.user.VerificationTokenService;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;

@Controller
@RequestMapping(Urls.USER)
class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    UserController(UserService userService,
                   VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public String verify(Model model, @RequestParam String token) {
        VerificationToken verificationToken = verificationTokenService.findByContent(token);
        if (verificationToken == null) {
            throw new ResourceNotFoundException("Token [" + token + "] is invalid");
        }

        User user = verificationToken.getUser();
        boolean activated = user.isActivated();
        if (!activated) {
            verificationTokenService.consume(verificationToken);
        } else {
            logger.debug("User [" + user.getUsername() + "] is already activated");
        }

        model.addAttribute("activated", activated);
        return "/user/verified";
    }

}
