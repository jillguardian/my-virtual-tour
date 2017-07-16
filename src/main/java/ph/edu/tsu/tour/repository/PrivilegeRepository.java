package ph.edu.tsu.tour.repository;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.domain.Privilege;

public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {
    Privilege findPrivilegeByName(String name);
}
