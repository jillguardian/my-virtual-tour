package ph.edu.tsu.tour.runtime.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
@EqualsAndHashCode
final class DropboxProperties {

    @Value("${application.storage.dropbox.access-token}")
    private String accessToken;

}
