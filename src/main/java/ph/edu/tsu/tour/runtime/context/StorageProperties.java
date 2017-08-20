package ph.edu.tsu.tour.runtime.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Getter
@EqualsAndHashCode
final class StorageProperties {

    @Value("${storage.base-uri}")
    private URI baseUri;
    private DropboxStorageProperties dropboxStorageProperties;

    @Autowired
    public StorageProperties(DropboxStorageProperties dropboxStorageProperties) {
        this.dropboxStorageProperties = dropboxStorageProperties;
    }

    @Service
    @Getter
    @EqualsAndHashCode
    static final class DropboxStorageProperties {

        @Value("${storage.dropbox.access-token}")
        private String accessToken;

    }

}
