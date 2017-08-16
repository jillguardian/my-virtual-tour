package ph.edu.tsu.tour.core;

public interface CrudService<T> {

    T findById(long id);

    Iterable<T> findAll();

    T save(T entity);

    boolean deleteById(long id);

    boolean exists(long id);

}
