package ph.edu.tsu.tour.core.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = Location.TABLE_NAME)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public abstract class Location implements Serializable {

    private static final long serialVersionUID = -7195733351894544107L;
    static final String TABLE_NAME = "locations";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty("UID")
    protected Long id;

    @Column(nullable = false)
    @JsonProperty("NAME")
    protected String name;

    @Convert(converter = UriPersistenceConverter.class)
    @JsonProperty("URL")
    protected URI website;

    @Column(name = "contact_number")
    @JsonProperty("TEL")
    protected String contactNumber;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cover_image_one_id", referencedColumnName = "id")
    @JsonProperty("IMAGE")
    protected Image coverImage;

    @Setter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "location_images",
            joinColumns = @JoinColumn(name = "location_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "image_id", referencedColumnName = "id"))
    @JsonProperty("IMAGES")
    protected Set<Image> images = new HashSet<>();

    @Column(name = "address_line_1")
    @JsonProperty("ADDRESS1")
    protected String addressLine1;

    @Column(name = "address_line_2")
    @JsonProperty("ADDRESS2")
    protected String addressLine2;

    @Column(nullable = false)
    @JsonProperty("CITY")
    protected String city;

    @Column(name = "zip_code", nullable = false)
    @JsonProperty("ZIP")
    protected String zipCode;

    @JsonIgnore
    @Convert(converter = GeoJsonObjectConverter.class)
    @Column(nullable = false)
    protected GeoJsonObject geometry;

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

}