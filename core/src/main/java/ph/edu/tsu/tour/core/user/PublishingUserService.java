package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;

public class PublishingUserService extends Observable implements UserService {

    private final UserService userService;

    public PublishingUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User save(User user) {
        boolean exists = user.getId() != null;
        User saved = userService.save(user);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        UserModifiedEvent userModifiedEvent = new UserModifiedEvent(action, saved);
        publish(userModifiedEvent);

        return saved;
    }

    @Override
    public Iterable<User> findAll() {
        return userService.findAll();
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userService.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userService.findByEmail(email);
    }

    @Override
    public boolean deleteById(long id) {
        User user = userService.findById(id);
        boolean deleted = userService.deleteById(id);
        if (deleted) {
            UserModifiedEvent userModifiedEvent = new UserModifiedEvent(EntityAction.DELETED, user);
            publish(userModifiedEvent);
        }
        return deleted;
    }

    protected void publish(UserModifiedEvent event) {
        setChanged();
        notifyObservers(event);
    }

}
