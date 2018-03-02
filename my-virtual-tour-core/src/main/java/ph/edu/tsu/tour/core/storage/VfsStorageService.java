package ph.edu.tsu.tour.core.storage;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.PubliclyAccessibleFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class VfsStorageService implements StreamingStorageService<URI, URI> {

    private static final Logger logger = LoggerFactory.getLogger(VfsStorageService.class);

    private FileSystemManager fileSystemManager;
    private FileSystemOptions fileSystemOptions;

    public VfsStorageService(FileSystemManager fileSystemManager, FileSystemOptions fileSystemOptions) {
        this.fileSystemManager = fileSystemManager;
        this.fileSystemOptions = fileSystemOptions;
    }

    @Override
    public void write(URI uri, byte[] bytes) throws IOException {
        FileObject destination = fileSystemManager.resolveFile(uri.toASCIIString(), fileSystemOptions);
        OutputStream outputStream = destination.getContent().getOutputStream();
        outputStream.write(bytes);
        outputStream.close();
    }

    @Override
    public void delete(URI uri) throws IOException {
        FileObject source = fileSystemManager.resolveFile(uri.toASCIIString(), fileSystemOptions);
        source.delete();
    }

    @Override
    public URI getStream(URI uri) throws IOException {
        FileObject fileObject = fileSystemManager.resolveFile(uri.toASCIIString(), fileSystemOptions);
        if (fileObject instanceof PubliclyAccessibleFileObject) {
            return PubliclyAccessibleFileObject.class.cast(fileObject).getPubliclyAccessibleUri();
        }
        return null;
    }
}
