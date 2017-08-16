package ph.edu.tsu.tour.core.access;

public interface AccessManagementService {

    Privilege savePrivilege(Privilege privilege);

    Role saveRole(Role role);

    User saveUser(User user);

    Iterable<Privilege> savePrivileges(Iterable<Privilege> privileges);

    Iterable<Role> saveRoles(Iterable<Role> roles);

    Iterable<User> saveUsers(Iterable<User> users);

    Iterable<Privilege> findAllPrivileges();

    Iterable<Role> findAllRoles();

    Iterable<User> findAllUsers();

    boolean deletePrivilegeById(long id);

    boolean deleteRoleById(long id);

    boolean deleteUserById(long id);

    Privilege findPrivilegeById(long id);

    Role findRoleById(long id);

    User findUserById(long id);

    Role findRoleByName(String name);

}
