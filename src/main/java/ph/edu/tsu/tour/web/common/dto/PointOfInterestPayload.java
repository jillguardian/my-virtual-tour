package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
public final class PointOfInterestPayload implements Serializable {

    private static final long serialVersionUID = -6972769675344484349L;

    private Long id;

    @NotNull(message = "{poi.name.blank.message}")
    @Size(min = 1, message = "{poi.name.blank.message}")
    private String name;

    private URI website;
    private String contactNumber;
    private String addressLine1;
    private String addressLine2;

    @NotNull(message = "{poi.city.blank.message}")
    @Size(min = 1, message = "{poi.city.blank.message}")
    private String city;

    @NotNull(message = "{poi.zip.blank.message}")
    @Size(min = 1, message = "{poi.zip.blank.message}")
    private String zipCode;

    @NotNull(message = "{poi.geometry.point.latitude.blank.message}")
    private Double latitude;

    @NotNull(message = "{poi.geometry.point.longitude.blank.message}")
    private Double longitude;

    private ImagePayload coverImage1;
    private ImagePayload coverImage2;
    private Set<ImagePayload> images;

}
