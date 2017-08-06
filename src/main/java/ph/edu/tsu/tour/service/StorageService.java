package ph.edu.tsu.tour.service;

import java.io.InputStream;

public interface StorageService<ID> {

    void write(ID id, byte[] bytes) throws Exception;
    void delete(ID id) throws Exception;

}
