package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import ph.edu.tsu.tour.core.location.Location;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class ImagePayload implements Serializable {

    private static final long serialVersionUID = -7570990199874211611L;

    /**
     * <p>The id of the {@link Location Location} that owns this image.</p>
     */
    private Long reference;

    private Long id;
    private String title;
    private String description;
    private Integer priority;
    private Set<String> tags = new HashSet<>();
    private MultipartFile file;
    private URI uri;

}
