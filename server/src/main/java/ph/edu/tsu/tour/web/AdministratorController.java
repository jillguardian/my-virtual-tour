package ph.edu.tsu.tour.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import ph.edu.tsu.tour.web.common.dto.AdministratorPayload;

import javax.validation.Valid;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@Controller
@RequestMapping(Urls.ADMINISTRATOR)
class AdministratorController {

    private AccessManagementService accessManagementService;

    @Autowired
    AdministratorController( AccessManagementService accessManagementService) {
        this.accessManagementService = accessManagementService;
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String save(Model model) {
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        model.addAttribute("administrator", AdministratorPayload.builder().build());
        return "access-management/administrator/one";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Model model,
                       @Valid @ModelAttribute("administrator") AdministratorPayload dto,
                       BindingResult bindingResult) {
        if (dto.getId() == null && dto.getPassword().isEmpty()) {
            bindingResult.rejectValue("password", "administrator.password.blank.message", "Password can't be blank.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
            return "access-management/administrator/one";
        }

        Administrator administrator = Administrator.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .roles(dto.getRoles().stream()
                        .map(name -> accessManagementService.findRoleByName(name))
                        .collect(Collectors.toSet()))
                .build();
        administrator = accessManagementService.saveAdministrator(administrator);
        return "redirect:" + Urls.ADMINISTRATOR + "/" + administrator.getId();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String deleteById(@RequestParam long id) {
        accessManagementService.deleteAdministratorById(id);
        return "redirect:" + Urls.ADMINISTRATOR;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String findById(@PathVariable long id, Model model) {
        Administrator administrator = accessManagementService.findAdministratorById(id);
        if (administrator == null) {
            throw new ResourceNotFoundException("Administrator with id [" + id + "] does not exist");
        }

        AdministratorPayload dto = AdministratorPayload.builder()
                .id(administrator.getId())
                .firstName(administrator.getFirstName())
                .lastName(administrator.getLastName())
                .username(administrator.getUsername())
                .roles(administrator.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
        model.addAttribute("administrator", dto);
        model.addAttribute("selectableRoles", accessManagementService.findAllRoles());
        return "access-management/administrator/one";
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public String findAuthenticated(Authentication authentication, Model model) {
        Administrator administrator = accessManagementService.findAdministratorByUsername(authentication.getName());
        return findById(administrator.getId(), model);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String findAll(Model model) {
        Iterable<Administrator> administrators = accessManagementService.findAllAdministrators();
        for (Administrator administrator : administrators) {
            administrator.setPassword(null);
        }

        model.addAttribute("administrators", administrators);
        return "access-management/administrator/all";
    }

}
