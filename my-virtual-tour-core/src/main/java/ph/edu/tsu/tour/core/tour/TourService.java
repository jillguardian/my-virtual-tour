package ph.edu.tsu.tour.core.tour;

import ph.edu.tsu.tour.core.CrudService;
import ph.edu.tsu.tour.core.user.User;

public interface TourService extends CrudService<Tour, Long> {
    Iterable<Tour> findByAuthor(User user);
}
