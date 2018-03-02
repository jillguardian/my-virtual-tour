package ph.edu.tsu.tour.core.access;

import org.springframework.data.repository.CrudRepository;

public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {
    Privilege findPrivilegeByName(String name);
}
