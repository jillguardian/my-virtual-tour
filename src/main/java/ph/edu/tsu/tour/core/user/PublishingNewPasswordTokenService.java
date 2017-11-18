package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;

public class PublishingNewPasswordTokenService extends Observable implements NewPasswordTokenService {
    private final NewPasswordTokenService newPasswordTokenService;

    public PublishingNewPasswordTokenService(NewPasswordTokenService newPasswordTokenService) {
        this.newPasswordTokenService = newPasswordTokenService;
    }

    @Override
    public NewPasswordToken save(NewPasswordToken newPasswordToken) {
        boolean exists = newPasswordToken.getId() != null;
        NewPasswordToken saved = newPasswordTokenService.save(newPasswordToken);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        NewPasswordTokenModifiedEvent newPasswordTokenModifiedEvent =
                new NewPasswordTokenModifiedEvent(action, saved);
        publish(newPasswordTokenModifiedEvent);

        return saved;
    }

    @Override
    public Iterable<NewPasswordToken> findAll() {
        return newPasswordTokenService.findAll();
    }

    @Override
    public NewPasswordToken findById(long id) {
        return newPasswordTokenService.findById(id);
    }

    @Override
    public NewPasswordToken findByUser(User user) {
        return newPasswordTokenService.findByUser(user);
    }

    @Override
    public NewPasswordToken findByContent(String content) {
        return newPasswordTokenService.findByContent(content);
    }

    @Override
    public boolean deleteById(long id) {
        NewPasswordToken newPasswordToken = newPasswordTokenService.findById(id);
        boolean deleted = newPasswordTokenService.deleteById(id);
        if (deleted) {
            NewPasswordTokenModifiedEvent newPasswordTokenModifiedEvent =
                    new NewPasswordTokenModifiedEvent(EntityAction.DELETED, newPasswordToken);
            publish(newPasswordTokenModifiedEvent);
        }
        return deleted;
    }

    @Override
    public NewPasswordToken produce(User user) {
        boolean exists = newPasswordTokenService.findByUser(user) != null;

        NewPasswordToken newPasswordToken = newPasswordTokenService.produce(user);
        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        NewPasswordTokenModifiedEvent newPasswordTokenModifiedEvent =
                new NewPasswordTokenModifiedEvent(action, newPasswordToken);
        publish(newPasswordTokenModifiedEvent);

        return newPasswordToken;
    }

    @Override
    public void consume(NewPasswordToken newPasswordToken) {
        newPasswordTokenService.consume(newPasswordToken);
    }

    private void publish(NewPasswordTokenModifiedEvent newPasswordTokenModifiedEvent) {
        setChanged();
        notifyObservers(newPasswordTokenModifiedEvent);
    }
    
}
