package ph.edu.tsu.tour.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.edu.tsu.tour.domain.Image;
import ph.edu.tsu.tour.repository.ImageRepository;
import ph.edu.tsu.tour.service.ImageService;

import javax.transaction.Transactional;

@Transactional
public class ImageServiceImpl implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    private ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image findById(long id) {
        return imageRepository.findOne(id);
    }

    @Override
    public Iterable<Image> findAll() {
        return imageRepository.findAll();
    }

    @Override
    public Image save(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public Iterable<Image> save(Iterable<Image> images) {
        return imageRepository.save(images);
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
