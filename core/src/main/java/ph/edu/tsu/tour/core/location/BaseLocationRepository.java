package ph.edu.tsu.tour.core.location;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseLocationRepository<T extends Location> extends CrudRepository<T, Long> {

    @Query("SELECT e FROM #{#entityName} e")
    @Override
    Iterable<T> findAll(Iterable<Long> ids);

}
