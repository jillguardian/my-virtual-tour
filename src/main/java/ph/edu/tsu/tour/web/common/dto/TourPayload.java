package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geojson.FeatureCollection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
public class TourPayload {

    private Long id;
    private String title;
    private String description;

    @NotNull
    @Size(min = 1)
    private Set<Long> locations;

}
