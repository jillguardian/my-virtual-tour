package ph.edu.tsu.tour.core.poi;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Collection;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

public class PublishingPointOfInterestService extends Observable implements PointOfInterestService {

    private PointOfInterestService pointOfInterestService;

    public PublishingPointOfInterestService(PointOfInterestService pointOfInterestService) {
        this.pointOfInterestService = pointOfInterestService;
    }

    @Override
    public PointOfInterest findById(long id) {
        return pointOfInterestService.findById(id);
    }

    @Override
    public Iterable<PointOfInterest> findAll() {
        return pointOfInterestService.findAll();
    }

    @Override
    public PointOfInterest save(PointOfInterest entity) {
        boolean exists = entity.getId() != null;
        PointOfInterest saved = pointOfInterestService.save(entity);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        PointOfInterestModifiedEvent pointOfInterestModifiedEvent = new PointOfInterestModifiedEvent(action, saved);
        publish(pointOfInterestModifiedEvent);

        return saved;
    }

    @Override
    public boolean deleteById(long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        boolean deleted = pointOfInterestService.deleteById(id);
        if (deleted) {
            PointOfInterestModifiedEvent  pointOfInterestModifiedEvent =
                    new PointOfInterestModifiedEvent(EntityAction.DELETED, poi);
            publish(pointOfInterestModifiedEvent);
        }
        return deleted;
    }

    @Override
    public boolean exists(long id) {
        return pointOfInterestService.exists(id);
    }

    protected void publish(PointOfInterestModifiedEvent event) {
        setChanged();
        notifyObservers(event);
    }

}
