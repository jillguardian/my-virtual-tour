package ph.edu.tsu.tour.core.access;

// TODO: Breakdown into multiple interfaces.
public interface AccessManagementService {

    Privilege savePrivilege(Privilege privilege);

    Role saveRole(Role role);

    Administrator saveAdministrator(Administrator administrator);

    Iterable<Privilege> savePrivileges(Iterable<Privilege> privileges);

    Iterable<Role> saveRoles(Iterable<Role> roles);

    Iterable<Administrator> saveAdministrators(Iterable<Administrator> administrators);

    Iterable<Privilege> findAllPrivileges();

    Iterable<Role> findAllRoles();

    Iterable<Administrator> findAllAdministrators();

    boolean deletePrivilegeById(long id);

    boolean deleteRoleById(long id);

    boolean deleteAdministratorById(long id);

    Privilege findPrivilegeById(long id);

    Privilege findPrivilegeByName(String name);

    Role findRoleById(long id);

    Administrator findAdministratorById(long id);

    Role findRoleByName(String name);

    Administrator findAdministratorByUsername(String username);

}
