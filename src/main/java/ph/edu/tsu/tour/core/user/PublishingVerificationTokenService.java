package ph.edu.tsu.tour.core.user;

import ph.edu.tsu.tour.core.EntityAction;

import java.util.Observable;

public class PublishingVerificationTokenService extends Observable implements VerificationTokenService {

    private final VerificationTokenService verificationTokenService;

    public PublishingVerificationTokenService(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public VerificationToken save(VerificationToken verificationToken) {
        boolean exists = verificationToken.getId() != null;
        VerificationToken saved = verificationTokenService.save(verificationToken);

        EntityAction action = exists ? EntityAction.MODIFIED : EntityAction.CREATED;
        VerificationTokenModifiedEvent verificationTokenModifiedEvent =
                new VerificationTokenModifiedEvent(action, saved);
        publish(verificationTokenModifiedEvent);

        return saved;
    }

    @Override
    public Iterable<VerificationToken> findAll() {
        return verificationTokenService.findAll();
    }

    @Override
    public VerificationToken findById(long id) {
        return verificationTokenService.findById(id);
    }

    @Override
    public VerificationToken findByUser(User user) {
        return verificationTokenService.findByUser(user);
    }

    @Override
    public VerificationToken findByContent(String content) {
        return verificationTokenService.findByContent(content);
    }

    @Override
    public VerificationToken create(User user) {
        return verificationTokenService.create(user);
    }

    @Override
    public boolean deleteById(long id) {
        VerificationToken verificationToken = verificationTokenService.findById(id);
        boolean deleted = verificationTokenService.deleteById(id);
        if (deleted) {
            VerificationTokenModifiedEvent verificationTokenModifiedEvent =
                    new VerificationTokenModifiedEvent(EntityAction.DELETED, verificationToken);
            publish(verificationTokenModifiedEvent);
        }
        return deleted;
    }

    private void publish(VerificationTokenModifiedEvent verificationTokenModifiedEvent) {
        setChanged();
        notifyObservers(verificationTokenModifiedEvent);
    }

}
