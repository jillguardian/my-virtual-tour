package ph.edu.tsu.tour.core.image;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class RawImage {

    private Long id;

    private String title;

    private String description;

    private InputStream inputStream;

    private Integer priority;

    private Set<String> tags = new HashSet<>();

}
