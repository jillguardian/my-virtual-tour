package ph.edu.tsu.tour.repository;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findRoleByName(String name);
}
