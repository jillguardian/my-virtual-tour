package ph.edu.tsu.tour.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;

import java.util.Locale;

@Controller
@RequestMapping(Urls.USER)
class UserController {

    private UserService userService;
    private MessageSource messageSource;

    @Autowired
    UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String findAll(Model model) {
        Iterable<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "user/all";
    }

    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public String enable(Locale locale,
                         RedirectAttributes redirectAttributes,
                         @RequestParam long id,
                         @RequestParam boolean enable) {
        User user = userService.findById(id);
        if (user == null) {
            String userNotFoundMessage = messageSource.getMessage("user.not-exists.message", new Object[] {id}, locale);
            redirectAttributes.addFlashAttribute("errors", new String[] { userNotFoundMessage });
        } else {
            user.setEnabled(enable);
            userService.save(user);

            String userSetEnabledMessage = messageSource.getMessage("user.update.message", null, locale);
            redirectAttributes.addFlashAttribute("modifiedUsers", new long[]{ id });
            redirectAttributes.addFlashAttribute("successes", new String[] { userSetEnabledMessage });
        }

        return "redirect:" + Urls.USER;
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public String changePassword(Locale locale,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam long id,
                                 @RequestParam String password) {
        User user = userService.findById(id);
        if (user == null) {
            String userNotFoundMessage = messageSource.getMessage("user.not-exists.message", new Object[] {id}, locale);
            redirectAttributes.addFlashAttribute("errors", new String[] { userNotFoundMessage });
        } else {
            user.setPassword(password);
            userService.save(user);

            String userSetEnabledMessage = messageSource.getMessage("user.update.message", null, locale);
            redirectAttributes.addFlashAttribute("modifiedUsers", new long[]{ id });
            redirectAttributes.addFlashAttribute("successes", new String[] { userSetEnabledMessage });
        }
        return "redirect:" + Urls.USER;
    }

}
