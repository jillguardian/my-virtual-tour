package ph.edu.tsu.tour.service;

public interface Service<T> {

    T findById(long id);

    Iterable<T> findAll();

    T save(T entity);

    Iterable<T> save(Iterable<T> entities);

    boolean deleteById(long id);

    boolean exists(long id);

}
