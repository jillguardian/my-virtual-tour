package ph.edu.tsu.tour.core.location;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;

public class PublishingLocationService extends Observable implements LocationService {

    private LocationService locationService;

    public PublishingLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public Location findById(Long id) {
        return locationService.findById(id);
    }

    @Override
    public Iterable<Location> findAll() {
        return locationService.findAll();
    }

    @Override
    public Iterable<Location> findAll(Iterable<Long> ids) {
        return locationService.findAll(ids);
    }

    @Override
    public Location save(Location entity) {
        boolean exists = entity.getId() != null;
        Location saved = locationService.save(entity);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        LocationModifiedEvent locationModifiedEvent = new LocationModifiedEvent(action, saved);
        publish(locationModifiedEvent);

        return saved;
    }

    @Override
    public boolean deleteById(Long id) {
        Location location = locationService.findById(id);
        boolean deleted = locationService.deleteById(id);
        if (deleted) {
            LocationModifiedEvent locationModifiedEvent =
                    new LocationModifiedEvent(EntityAction.DELETED, location);
            publish(locationModifiedEvent);
        }
        return deleted;
    }

    @Override
    public boolean exists(Long id) {
        return locationService.exists(id);
    }

    protected void publish(LocationModifiedEvent event) {
        setChanged();
        notifyObservers(event);
    }

}
