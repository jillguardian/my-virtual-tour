package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public abstract class LocationPayload implements Serializable {

    private static final long serialVersionUID = -6972769675344484349L;

    protected Long id;

    @NotNull(message = "{location.name.blank.message}")
    @Size(min = 1, message = "{location.name.blank.message}")
    protected String name;

    protected URI website;
    protected String contactNumber;
    protected String addressLine1;
    protected String addressLine2;

    @NotNull(message = "{location.city.blank.message}")
    @Size(min = 1, message = "{location.city.blank.message}")
    protected String city;

    @NotNull(message = "{location.zip.blank.message}")
    @Size(min = 1, message = "{location.zip.blank.message}")
    protected String zipCode;

    @NotNull(message = "{location.geometry.point.latitude.blank.message}")
    protected Double latitude;

    @NotNull(message = "{location.geometry.point.longitude.blank.message}")
    protected Double longitude;

    protected ImagePayload coverImage1;
    protected ImagePayload coverImage2;
    protected Set<ImagePayload> images;

}
