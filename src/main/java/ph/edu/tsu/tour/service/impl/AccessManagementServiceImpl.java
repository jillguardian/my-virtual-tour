package ph.edu.tsu.tour.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import ph.edu.tsu.tour.domain.Privilege;
import ph.edu.tsu.tour.domain.Role;
import ph.edu.tsu.tour.domain.User;
import ph.edu.tsu.tour.repository.PrivilegeRepository;
import ph.edu.tsu.tour.repository.RoleRepository;
import ph.edu.tsu.tour.repository.UserRepository;
import ph.edu.tsu.tour.service.AccessManagementService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Transactional
public class AccessManagementServiceImpl implements AccessManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AccessManagementServiceImpl.class);

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PrivilegeRepository privilegeRepository;

    public AccessManagementServiceImpl(PrivilegeRepository privilegeRepository,
                                       RoleRepository roleRepository,
                                       UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        this.privilegeRepository = Objects.requireNonNull(privilegeRepository, "[privilegeRepository] must be set");
        this.roleRepository = Objects.requireNonNull(roleRepository, "[roleRepository] must be set");
        this.userRepository = Objects.requireNonNull(userRepository, "[userRepository] must be set");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "[passwordEncoder] must be set");
    }

    @Override
    public Privilege savePrivilege(Privilege privilege) {
        if (privilegeRepository.findPrivilegeByName(privilege.getName()) != null) {
            throw new IllegalArgumentException("Privilege [" + privilege.getName() + "] already exists");
        }
        return privilegeRepository.save(privilege);
    }

    @Override
    public Role saveRole(Role role) {
        if (roleRepository.findRoleByName(role.getName()) != null) {
            throw new IllegalArgumentException("Role [" + role.getName() + "] already exists");
        }
        for (Privilege privilege : role.getPrivileges()) {
            if (!privilegeRepository.exists(privilege.getId())) {
                throw new IllegalArgumentException("Privilege with id [" + privilege.getId() + "] does not exist");
            }
        }
        return roleRepository.save(role);
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null && userRepository.findUserByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] already exists");
        }
        for (Role role : user.getRoles()) {
            if (!roleRepository.exists(role.getId())) {
                throw new IllegalArgumentException("Role with id [" + role.getId() + "] does not exist");
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
    public Iterable<User> saveUsers(Iterable<User> users) {
        Collection<User> saved = new ArrayList<>();
        for (User user : users) {
            user = saveUser(user);
            saved.add(user);
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
    public Iterable<User> findAllUsers() {
        return userRepository.findAll();
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
    public boolean deleteUserById(long id) {
        if (userRepository.exists(id)) {
            userRepository.delete(id);
            return !userRepository.exists(id);
        }
        return false;
    }

    @Override
    public Privilege findPrivilegeById(long id) {
        return privilegeRepository.findOne(id);
    }

    @Override
    public Role findRoleById(long id) {
        return roleRepository.findOne(id);
    }

    @Override
    public User findUserById(long id) {
        return userRepository.findOne(id);
    }

    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }

}
