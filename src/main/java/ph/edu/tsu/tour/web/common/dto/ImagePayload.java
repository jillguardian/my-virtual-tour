package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.net.URI;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class ImagePayload implements Serializable {

    private static final long serialVersionUID = -7570990199874211611L;

    /**
     * <p>The id of the {@link ph.edu.tsu.tour.core.poi.PointOfInterest PointOfInterest} that owns this image.</p>
     */
    private Long reference;

    private Long id;
    private String title;
    private String description;
    private MultipartFile file;
    private URI uri;

}
