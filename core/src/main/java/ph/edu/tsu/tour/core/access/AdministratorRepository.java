package ph.edu.tsu.tour.core.access;

import org.springframework.data.repository.CrudRepository;

public interface AdministratorRepository extends CrudRepository<Administrator, Long> {
    Administrator findAdministratorByUsername(String username);
}
