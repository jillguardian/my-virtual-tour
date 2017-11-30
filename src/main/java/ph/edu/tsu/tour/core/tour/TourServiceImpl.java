package ph.edu.tsu.tour.core.tour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.core.location.LocationServiceImpl;
import ph.edu.tsu.tour.core.user.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class TourServiceImpl implements TourService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    private TourRepository tourRepository;

    public TourServiceImpl(EntityManager entityManager, TourRepository tourRepository) {
        this.entityManager = entityManager;
        this.tourRepository = tourRepository;
    }

    @Override
    public Tour findById(Long id) {
        Tour found = tourRepository.findOne(id);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public Iterable<Tour> findAll() {
        Iterable<Tour> all = tourRepository.findAll();
        for (Tour tour : all) {
            entityManager.detach(tour);
        }
        return all;
    }

    @Override
    public Iterable<Tour> findAll(Iterable<Long> ids) {
        Iterable<Tour> all = tourRepository.findAll(ids);
        for (Tour tour : all) {
            entityManager.detach(tour);
        }
        return all;
    }

    @Override
    public Tour save(Tour tour) {
        return tourRepository.save(tour);
    }

    @Override
    public boolean deleteById(Long id) {
        tourRepository.delete(id);
        boolean exists = tourRepository.exists(id);
        if (exists) {
            logger.error("Couldn't delete location with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public boolean exists(Long id) {
        return tourRepository.exists(id);
    }

    @Override
    public Iterable<Tour> findByAuthor(User author) {
        return tourRepository.findByAuthor(author);
    }
}
