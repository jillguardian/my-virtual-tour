package ph.edu.tsu.tour.core.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Objects;

@Transactional
public class LocationServiceImpl<T extends Location> implements LocationService<T> {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    private BaseLocationRepository<T> locationRepository;

    public LocationServiceImpl(EntityManager entityManager, BaseLocationRepository<T> locationRepository) {
        this.entityManager = entityManager;
        this.locationRepository = Objects.requireNonNull(locationRepository, "[locationRepository] must be set");
    }

    @Override
    public T findById(Long id) {
        T found = locationRepository.findOne(id);
        if (found != null) {
            entityManager.detach(found);
        }
        return found;
    }

    @Override
    public Iterable<T> findAll() {
        Iterable<T> all = locationRepository.findAll();
        for (Location location : all) {
            entityManager.detach(location);
        }
        return all;
    }

    @Override
    public Iterable<T> findAll(Iterable<Long> ids) {
        Iterable<T> all = locationRepository.findAll(ids);
        for (Location location : all) {
            entityManager.detach(location);
        }
        return all;
    }

    @Override
    public T save(T entity) {
        return locationRepository.save(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        locationRepository.delete(id);
        boolean exists = locationRepository.exists(id);
        if (exists) {
            logger.error("Couldn't delete location with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public boolean exists(Long id) {
        return locationRepository.exists(id);
    }

}
