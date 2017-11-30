package ph.edu.tsu.tour.core.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Tolerate;
import org.geojson.GeoJsonObject;
import ph.edu.tsu.tour.core.common.converter.GeoJsonObjectConverter;
import ph.edu.tsu.tour.core.common.converter.UriPersistenceConverter;
import ph.edu.tsu.tour.core.image.Image;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Entity
@Table(name = Location.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Location implements Serializable {

    private static final long serialVersionUID = -7195733351894544107L;
    static final String TABLE_NAME = "locations";

    @Id
    @GeneratedValue
    @JsonProperty("UID")
    private Long id;

    @Column(nullable = false)
    @JsonProperty("NAME")
    private String name;

    @Convert(converter = UriPersistenceConverter.class)
    @JsonProperty("URL")
    private URI website;

    @Column(name = "contact_number")
    @JsonProperty("TEL")
    private String contactNumber;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="cover_image_one_id", referencedColumnName = "id")
    @JsonProperty("IMAGE")
    private Image coverImage1;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="cover_image_two_id", referencedColumnName = "id")
    @JsonProperty("IMAGEBACK")
    private Image coverImage2;

    @Setter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "location_images",
            joinColumns = @JoinColumn(name = "location_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "image_id", referencedColumnName = "id"))
    @JsonProperty("IMAGES")
    private Set<Image> images = new HashSet<>();

    @Column(name = "address_line_1")
    @JsonProperty("ADDRESS1")
    private String addressLine1;

    @Column(name = "address_line_2")
    @JsonProperty("ADDRESS2")
    private String addressLine2;

    @Column(nullable = false)
    @JsonProperty("CITY")
    private String city;

    @Column(name = "zip_code", nullable = false)
    @JsonProperty("ZIP")
    private String zipCode;

    @JsonIgnore
    @Convert(converter = GeoJsonObjectConverter.class)
    @Column(nullable = false)
    private GeoJsonObject geometry;

    @Tolerate
    protected Location() {
        // A default constructor to make JPA happy.
    }

    public Set<Image> getImages() {
        return new HashSet<>(images);
    }

    public void addImage(Image image) {
        if (images.contains(image)) {
            return;
        }
        images.add(image);
    }

    public void removeImage(Image image) {
        images.remove(image);
    }

    public static class Builder {
        private Set<Image> images = new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Location location) {
        return new Builder()
                .id(location.getId())
                .name(location.getName())
                .website(location.getWebsite())
                .contactNumber(location.getContactNumber())
                .addressLine1(location.getAddressLine1())
                .addressLine2(location.getAddressLine2())
                .city(location.getCity())
                .zipCode(location.getZipCode())
                .geometry(location.getGeometry())
                .coverImage1(location.getCoverImage1())
                .coverImage2(location.getCoverImage2())
                .images(location.getImages());
    }

}