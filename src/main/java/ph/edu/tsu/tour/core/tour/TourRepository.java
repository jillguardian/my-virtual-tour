package ph.edu.tsu.tour.core.tour;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.core.user.User;

public interface TourRepository extends CrudRepository<Tour, Long> {
    Iterable<Tour> findByAuthor(User author);
}
