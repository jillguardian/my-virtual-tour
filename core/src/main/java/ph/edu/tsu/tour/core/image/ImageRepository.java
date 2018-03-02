package ph.edu.tsu.tour.core.image;

import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<Image, Long> {

    Iterable<Image> findByTitleIgnoreCaseContaining(String title);
    Iterable<Image> findByDescriptionIgnoreCaseContaining(String title);

}
