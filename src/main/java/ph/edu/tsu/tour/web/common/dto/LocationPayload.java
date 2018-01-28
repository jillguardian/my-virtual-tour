package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.edu.tsu.tour.web.common.validator.Within;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Within(query = "Tarlac", types = "region", countries = "PH", message = "{poi.geometry.point.beyond-limit.message}")
public final class LocationPayload implements Serializable {

    private static final long serialVersionUID = -6972769675344484349L;

    private Long id;

    @NotNull(message = "{location.name.blank.message}")
    @Size(min = 1, message = "{location.name.blank.message}")
    private String name;

    private URI website;
    private String contactNumber;
    private String addressLine1;
    private String addressLine2;

    @NotNull(message = "{location.city.blank.message}")
    @Size(min = 1, message = "{location.city.blank.message}")
    private String city;

    @NotNull(message = "{location.zip.blank.message}")
    @Size(min = 1, message = "{location.zip.blank.message}")
    private String zipCode;

    @NotNull(message = "{location.geometry.point.latitude.blank.message}")
    private Double latitude;

    @NotNull(message = "{location.geometry.point.longitude.blank.message}")
    private Double longitude;

    private ImagePayload coverImage1;
    private ImagePayload coverImage2;
    private Set<ImagePayload> images;

}
