package ph.edu.tsu.tour.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileSystemStorageService implements StreamingStorageService<Path, Path> {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileSystemStorageService.class);

    @Override
    public void write(Path path, byte[] bytes) throws Exception {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
            logger.trace("Created [" + parent + "]");
        }

        Files.write(path, bytes);
    }

    @Override
    public void delete(Path path) throws Exception {
        Files.deleteIfExists(path);
    }

    @Override
    public Path getStream(Path path) throws Exception {
        return path;
    }

}
