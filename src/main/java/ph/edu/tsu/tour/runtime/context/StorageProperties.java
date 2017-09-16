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

    @Value("${application.storage.default-directory}")
    private URI defaultDirectory;
    private DropboxProperties dropboxProperties;

    @Autowired
    public StorageProperties(DropboxProperties dropboxProperties) {
        this.dropboxProperties = dropboxProperties;
    }

}
