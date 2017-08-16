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
final class VfsProperties {

    @Value("${storage.base-uri}")
    private URI baseUri;
    private DropboxVfsProperties dropboxVfsProperties;

    @Autowired
    public VfsProperties(DropboxVfsProperties dropboxVfsProperties) {
        this.dropboxVfsProperties = dropboxVfsProperties;
    }

    @Service
    @Getter
    @EqualsAndHashCode
    static final class DropboxVfsProperties {

        @Value("${storage.dropbox.access-token}")
        private String accessToken;

    }

}
