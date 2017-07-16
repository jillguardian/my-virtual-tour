package ph.edu.tsu.tour.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.geojson.GeoJsonObject;
import ph.edu.tsu.tour.common.converter.GeoJsonObjectConverter;
import ph.edu.tsu.tour.common.converter.UriPersistenceConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@Entity
@Table(name = PointOfInterest.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class PointOfInterest implements Serializable {

    private static final long serialVersionUID = -7195733351894544107L;
    static final String TABLE_NAME = "points_of_interest";

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

    @ElementCollection
    @CollectionTable(name = "points_of_interest_gallery", joinColumns = @JoinColumn(name = "id"))
    @Convert(converter = UriPersistenceConverter.class)
    @JsonProperty("IMAGES")
    private Set<URI> images;

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
    protected PointOfInterest() {
        // A default constructor to make JPA happy.
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(PointOfInterest pointOfInterest) {
        return new Builder()
                .id(pointOfInterest.getId())
                .name(pointOfInterest.getName())
                .website(pointOfInterest.getWebsite())
                .contactNumber(pointOfInterest.getContactNumber())
                .images(pointOfInterest.getImages())
                .addressLine1(pointOfInterest.getAddressLine1())
                .addressLine2(pointOfInterest.getAddressLine2())
                .city(pointOfInterest.getCity())
                .zipCode(pointOfInterest.getZipCode())
                .geometry(pointOfInterest.getGeometry());
    }

}