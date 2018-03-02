package ph.edu.tsu.tour.core.location;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;

public class PublishingLocationService<T extends Location> extends Observable implements LocationService<T> {

    private LocationService<T> locationService;

    public PublishingLocationService(LocationService<T> locationService) {
        this.locationService = locationService;
    }

    @Override
    public T findById(Long id) {
        return locationService.findById(id);
    }

    @Override
    public Iterable<T> findAll() {
        return locationService.findAll();
    }

    @Override
    public Iterable<T> findAll(Iterable<Long> ids) {
        return locationService.findAll(ids);
    }

    @Override
    public T save(T entity) {
        boolean exists = entity.getId() != null;
        T saved = locationService.save(entity);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        LocationModifiedEvent locationModifiedEvent = new LocationModifiedEvent(action, saved);
        publish(locationModifiedEvent);

        return saved;
    }

    @Override
    public boolean deleteById(Long id) {
        T location = locationService.findById(id);
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
