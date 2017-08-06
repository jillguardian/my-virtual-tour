package ph.edu.tsu.tour.repository;

import org.springframework.data.repository.CrudRepository;
import ph.edu.tsu.tour.domain.Image;

public interface ImageRepository extends CrudRepository<Image, Long> {

    Iterable<Image> findByTitleIgnoreCaseContaining(String title);
    Iterable<Image> findByDescriptionIgnoreCaseContaining(String title);

}
