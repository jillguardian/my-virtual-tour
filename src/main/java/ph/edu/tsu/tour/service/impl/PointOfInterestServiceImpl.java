package ph.edu.tsu.tour.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.domain.PointOfInterest;
import ph.edu.tsu.tour.repository.PointOfInterestRepository;
import ph.edu.tsu.tour.service.PointOfInterestService;

import javax.transaction.Transactional;
import java.util.Objects;

@Transactional
public class PointOfInterestServiceImpl implements PointOfInterestService {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestServiceImpl.class);

    private PointOfInterestRepository poiRepository;

    public PointOfInterestServiceImpl(PointOfInterestRepository poiRepository) {
        this.poiRepository = Objects.requireNonNull(poiRepository, "[poiRepository] must be set");
    }

    @Override
    public PointOfInterest findById(long id) {
        return poiRepository.findOne(id);
    }

    @Override
    public Iterable<PointOfInterest> findAll() {
        return poiRepository.findAll();
    }

    @Override
    public PointOfInterest save(PointOfInterest entity) {
        return poiRepository.save(entity);
    }

    @Override
    public Iterable<PointOfInterest> save(Iterable<PointOfInterest> entities) {
        return poiRepository.save(entities);
    }

    @Override
    public boolean deleteById(long id) {
        poiRepository.delete(id);
        boolean exists = poiRepository.exists(id);
        if (exists) {
            logger.error("Couldn't delete point of interest with id [" + id + "]");
        }
        return exists;
    }

    @Override
    public boolean exists(long id) {
        return poiRepository.exists(id);
    }

}
