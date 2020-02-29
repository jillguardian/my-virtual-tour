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

    @Value("${storage.default-directory}")
    private URI defaultDirectory;

    private DropboxProperties dropboxProperties;
    private AmazonS3Properties amazonS3Properties;

    @Autowired
    public StorageProperties(DropboxProperties dropboxProperties, AmazonS3Properties amazonS3Properties) {
        this.dropboxProperties = dropboxProperties;
        this.amazonS3Properties = amazonS3Properties;
    }

}
