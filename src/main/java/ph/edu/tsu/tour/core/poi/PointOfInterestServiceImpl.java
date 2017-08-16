package ph.edu.tsu.tour.core.poi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Objects;

@Transactional
public class PointOfInterestServiceImpl implements PointOfInterestService {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    private PointOfInterestRepository poiRepository;

    public PointOfInterestServiceImpl(EntityManager entityManager, PointOfInterestRepository poiRepository) {
        this.poiRepository = Objects.requireNonNull(poiRepository, "[poiRepository] must be set");
    }

    @Override
    public PointOfInterest findById(long id) {
        PointOfInterest found = poiRepository.findOne(id);
        entityManager.detach(found);
        return found;
    }

    @Override
    public Iterable<PointOfInterest> findAll() {
        Iterable<PointOfInterest> all = poiRepository.findAll();
        for (PointOfInterest poi : all) {
            entityManager.detach(poi);
        }
        return all;
    }

    @Override
    public PointOfInterest save(PointOfInterest entity) {
        return poiRepository.save(entity);
    }

    @Override
    public boolean deleteById(long id) {
        poiRepository.delete(id);
        boolean exists = poiRepository.exists(id);
        if (exists) {
            logger.error("Couldn't delete point of interest with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public boolean exists(long id) {
        return poiRepository.exists(id);
    }

}