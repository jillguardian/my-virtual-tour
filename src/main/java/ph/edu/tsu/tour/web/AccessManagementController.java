package ph.edu.tsu.tour.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ph.edu.tsu.tour.core.access.Role;
import ph.edu.tsu.tour.core.access.User;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.core.access.AccessManagementService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequestMapping(Urls.ACCESS_MANAGEMENT)
public class AccessManagementController {

    private AccessManagementService accessManagementService;

    @Autowired
    public AccessManagementController(AccessManagementService accessManagementService) {
        this.accessManagementService = accessManagementService;
    }

    @RequestMapping(value = "/user/new", method = RequestMethod.GET)
    public String saveUser(Model model) {
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        model.addAttribute("user", UserDto.builder().build());
        return "access-management/user/one";
    }

    @RequestMapping(value = "/user/save", method = RequestMethod.POST)
    public String saveUser(Model model, @Valid @ModelAttribute("user") UserDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
            return "access-management/user/one";
        }

        User user = User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .roles(dto.getRoles().stream()
                        .map(name -> accessManagementService.findRoleByName(name))
                        .collect(Collectors.toSet()))
                .build();
        user = accessManagementService.saveUser(user);
        return "redirect:" + Urls.ACCESS_MANAGEMENT + "/user/" + user.getId();
    }

    @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
    public String deleteUserById(@RequestParam long id) {
        accessManagementService.deleteUserById(id);
        return "redirect:" + Urls.ACCESS_MANAGEMENT + "/user/";
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public String findUserById(@PathVariable long id, Model model) {
        User user = accessManagementService.findUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User with id [" + id + "] does not exist");
        }

        UserDto dto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
        model.addAttribute("user", dto);
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        model.addAttribute("exists", true);
        return "access-management/user/one";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String findUsers(Model model) {
        model.addAttribute("users", accessManagementService.findAllUsers());
        return "access-management/user/all";
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    private static final class UserDto {

        private Long id;

        @NotNull
        @Size(min = 1)
        private String username;

        @NotNull
        @Size(min = 1)
        private String password;

        @NotNull
        @Size(min = 1)
        @Singular
        private Collection<String> roles;

    }

}
