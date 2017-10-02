package ph.edu.tsu.tour.core;

public interface CrudService<T, ID> {

    T findById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAll(Iterable<ID> ids);

    T save(T entity);

    boolean deleteById(ID id);

    boolean exists(ID id);

}
