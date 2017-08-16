package ph.edu.tsu.tour.core.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Transactional
public class ImageServiceImpl implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    private ImageRepository imageRepository;

    public ImageServiceImpl(EntityManager entityManager, ImageRepository imageRepository) {
        this.entityManager = entityManager;
        this.imageRepository = imageRepository;
    }

    @Override
    public Image findById(long id) {
        Image found = imageRepository.findOne(id);
        entityManager.detach(found);
        return found;
    }

    @Override
    public Iterable<Image> findAll() {
        Iterable<Image> all = imageRepository.findAll();
        for (Image image : all) {
            entityManager.detach(image);
        }
        return all;
    }

    @Override
    public Image save(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public boolean deleteById(long id) {
        imageRepository.delete(id);
        boolean exists = imageRepository.exists(id);
        if (exists) {
            logger.error("Unable to delete image with id [" + id + "]");
        }
        return !exists;
    }

    @Override
    public boolean exists(long id) {
        return imageRepository.exists(id);
    }

}
