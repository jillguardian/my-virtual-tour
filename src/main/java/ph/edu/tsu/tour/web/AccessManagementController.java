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
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.Administrator;
import ph.edu.tsu.tour.core.access.Role;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequestMapping(Urls.ACCESS_MANAGEMENT)
class AccessManagementController {

    private AccessManagementService accessManagementService;

    @Autowired
    AccessManagementController(AccessManagementService accessManagementService) {
        this.accessManagementService = accessManagementService;
    }

    @RequestMapping(value = "/administrator/new", method = RequestMethod.GET)
    public String saveAdministrator(Model model) {
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        model.addAttribute("administrator", AdministratorDto.builder().build());
        return "access-management/administrator/one";
    }

    @RequestMapping(value = "/administrator/save", method = RequestMethod.POST)
    public String saveAdministrator(Model model,
                                    @Valid @ModelAttribute("administrator") AdministratorDto dto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
            return "access-management/administrator/one";
        }

        Administrator administrator = Administrator.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .roles(dto.getRoles().stream()
                        .map(name -> accessManagementService.findRoleByName(name))
                        .collect(Collectors.toSet()))
                .build();
        administrator = accessManagementService.saveAdministrator(administrator);
        return "redirect:" + Urls.ACCESS_MANAGEMENT + "/administrator/" + administrator.getId();
    }

    @RequestMapping(value = "/administrator/delete", method = RequestMethod.POST)
    public String deleteAdministratorById(@RequestParam long id) {
        accessManagementService.deleteAdministratorById(id);
        return "redirect:" + Urls.ACCESS_MANAGEMENT + "/administrator/";
    }

    @RequestMapping(value = "/administrator/{id}", method = RequestMethod.GET)
    public String findAdministratorById(@PathVariable long id, Model model) {
        Administrator administrator = accessManagementService.findAdministratorById(id);
        if (administrator == null) {
            throw new ResourceNotFoundException("Administrator with id [" + id + "] does not exist");
        }

        AdministratorDto dto = AdministratorDto.builder()
                .id(administrator.getId())
                .username(administrator.getUsername())
                .password(administrator.getPassword())
                .roles(administrator.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
        model.addAttribute("administrator", dto);
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        model.addAttribute("exists", true);
        return "access-management/administrator/one";
    }

    @RequestMapping(value = "/administrator", method = RequestMethod.GET)
    public String findAdministrators(Model model) {
        model.addAttribute("administrators", accessManagementService.findAllAdministrators());
        return "access-management/administrator/all";
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    private static final class AdministratorDto {

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
