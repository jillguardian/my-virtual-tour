package ph.edu.tsu.tour.core.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Transactional
public class AccessManagementServiceImpl implements AccessManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AccessManagementServiceImpl.class);

    private PasswordEncoder passwordEncoder;
    private AdministratorRepository administratorRepository;
    private RoleRepository roleRepository;
    private PrivilegeRepository privilegeRepository;

    public AccessManagementServiceImpl(PrivilegeRepository privilegeRepository,
                                       RoleRepository roleRepository,
                                       AdministratorRepository administratorRepository,
                                       PasswordEncoder passwordEncoder) {
        this.privilegeRepository = Objects.requireNonNull(privilegeRepository, "[privilegeRepository] must be set");
        this.roleRepository = Objects.requireNonNull(roleRepository, "[roleRepository] must be set");
        this.administratorRepository = Objects.requireNonNull(administratorRepository, "[administratorRepository] must be set");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "[passwordEncoder] must be set");
    }

    @Override
    public Privilege savePrivilege(Privilege privilege) {
        return privilegeRepository.save(privilege);
    }

    @Override
    public Role saveRole(Role role) {
        for (Privilege privilege : role.getPrivileges()) {
            if (!privilegeRepository.exists(privilege.getId())) {
                throw new IllegalArgumentException("Privilege with id [" + privilege.getId() + "] does not exist");
            }
        }
        return roleRepository.save(role);
    }

    @Override
    public Administrator saveAdministrator(Administrator administrator) {
        if (administrator.getId() == null
                && administratorRepository.findAdministratorByUsername(administrator.getUsername()) != null) {
            throw new IllegalArgumentException("Administrator [" + administrator.getUsername() + "] already exists");
        }
        for (Role role : administrator.getRoles()) {
            if (!roleRepository.exists(role.getId())) {
                throw new IllegalArgumentException("Role with id [" + role.getId() + "] does not exist");
            }
        }
        if (administrator.getId() != null && administrator.getPassword().isEmpty()) {
            Administrator previous = administratorRepository.findOne(administrator.getId());
            administrator.setPassword(previous.getPassword());
        } else {
            administrator.setPassword(passwordEncoder.encode(administrator.getPassword()));
        }
        return administratorRepository.save(administrator);
    }

    @Override
    public Iterable<Privilege> savePrivileges(Iterable<Privilege> privileges) {
        Collection<Privilege> saved = new ArrayList<>();
        for (Privilege privilege : privileges) {
            privilege = savePrivilege(privilege);
            saved.add(privilege);
        }
        return saved;
    }

    @Override
    public Iterable<Role> saveRoles(Iterable<Role> roles) {
        Collection<Role> saved = new ArrayList<>();
        for (Role role : roles) {
            role = saveRole(role);
            saved.add(role);
        }
        return saved;
    }

    @Override
    public Iterable<Administrator> saveAdministrators(Iterable<Administrator> administrators) {
        Collection<Administrator> saved = new ArrayList<>();
        for (Administrator administrator : administrators) {
            administrator = saveAdministrator(administrator);
            saved.add(administrator);
        }
        return saved;
    }

    @Override
    public Iterable<Privilege> findAllPrivileges() {
        return privilegeRepository.findAll();
    }

    @Override
    public Iterable<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Iterable<Administrator> findAllAdministrators() {
        return administratorRepository.findAll();
    }

    @Override
    public boolean deletePrivilegeById(long id) {
        if (privilegeRepository.exists(id)) {
            privilegeRepository.delete(id);
            return !privilegeRepository.exists(id);
        }
        return false;
    }

    @Override
    public boolean deleteRoleById(long id) {
        if (roleRepository.exists(id)) {
            roleRepository.delete(id);
            return !roleRepository.exists(id);
        }
        return false;
    }

    @Override
    public boolean deleteAdministratorById(long id) {
        if (administratorRepository.exists(id)) {
            administratorRepository.delete(id);
            return !administratorRepository.exists(id);
        }
        return false;
    }

    @Override
    public Privilege findPrivilegeById(long id) {
        return privilegeRepository.findOne(id);
    }

    @Override
    public Privilege findPrivilegeByName(String name) {
        return privilegeRepository.findPrivilegeByName(name);
    }

    @Override
    public Role findRoleById(long id) {
        return roleRepository.findOne(id);
    }

    @Override
    public Administrator findAdministratorById(long id) {
        return administratorRepository.findOne(id);
    }

    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }

    @Override
    public Administrator findAdministratorByUsername(String username) {
        return administratorRepository.findAdministratorByUsername(username);
    }

}
