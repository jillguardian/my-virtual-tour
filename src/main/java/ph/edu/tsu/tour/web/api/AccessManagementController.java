package ph.edu.tsu.tour.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ph.edu.tsu.tour.web.Urls;
import ph.edu.tsu.tour.domain.Privilege;
import ph.edu.tsu.tour.domain.Role;
import ph.edu.tsu.tour.domain.User;
import ph.edu.tsu.tour.service.AccessManagementService;

@RestController("restAccessManagementController")
@RequestMapping(Urls.REST_ACCESS_MANAGEMENT)
public class AccessManagementController {

    private AccessManagementService accessManagementService;

    @Autowired
    public AccessManagementController(AccessManagementService accessManagementService) {
        this.accessManagementService = accessManagementService;
    }

    @RequestMapping(value = "privilege", method = RequestMethod.POST)
    public ResponseEntity<Privilege> savePrivilege(@RequestBody Privilege privilege) {
        Privilege saved = accessManagementService.savePrivilege(privilege);
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "role", method = RequestMethod.POST)
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        Role saved = accessManagementService.saveRole(role);
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "user", method = RequestMethod.POST)
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        User saved = accessManagementService.saveUser(user);
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "privilege", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Privilege>> findAllPrivileges() {
        return ResponseEntity.ok(accessManagementService.findAllPrivileges());
    }

    @RequestMapping(value = "role", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Role>> findAllRoles() {
        return ResponseEntity.ok(accessManagementService.findAllRoles());
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ResponseEntity<Iterable<User>> findAllUsers() {
        return ResponseEntity.ok(accessManagementService.findAllUsers());
    }

    @RequestMapping(value = "privilege/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Privilege> deletePrivilegeById(@PathVariable long id) {
        Privilege found = accessManagementService.findPrivilegeById(id);
        if (found != null) {
            boolean deleted = accessManagementService.deletePrivilegeById(id);
            if (deleted) {
                return ResponseEntity.ok(found);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @RequestMapping(value = "role/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Role> deleteRoleById(@PathVariable long id) {
        Role found = accessManagementService.findRoleById(id);
        if (found != null) {
            boolean deleted = accessManagementService.deleteRoleById(id);
            if (deleted) {
                return ResponseEntity.ok(found);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @RequestMapping(value = "user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteUserById(@PathVariable long id) {
        User found = accessManagementService.findUserById(id);
        if (found != null) {
            boolean deleted = accessManagementService.deleteUserById(id);
            if (deleted) {
                return ResponseEntity.ok(found);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
