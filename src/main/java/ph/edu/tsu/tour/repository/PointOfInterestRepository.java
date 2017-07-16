package ph.edu.tsu.tour.repository;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.domain.PointOfInterest;

public interface PointOfInterestRepository extends CrudRepository<PointOfInterest, Long> {
}
