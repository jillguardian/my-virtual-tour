package ph.edu.tsu.tour.core.image;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class RawImage {

    private Long id;

    private String title;

    private String description;

    private InputStream inputStream;

}
