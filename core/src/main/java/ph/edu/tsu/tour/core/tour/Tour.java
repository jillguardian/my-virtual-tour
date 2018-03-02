package ph.edu.tsu.tour.core.tour;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Tolerate;
import ph.edu.tsu.tour.core.common.converter.OffsetDateTimePersistenceConverter;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.user.User;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Entity
@Table(name = Tour.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Tour implements Serializable {

    private static final long serialVersionUID = 135140795254828376L;
    static final String TABLE_NAME = "tours";

    @Tolerate
    protected Tour() {

    }

    @Id
    @GeneratedValue
    @JsonProperty("ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User author;

    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tour_locations",
            joinColumns = @JoinColumn(name = "tour_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "location_id", referencedColumnName = "id"))
    private Set<Location> locations;

    private String title;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimePersistenceConverter.class)
    private OffsetDateTime created;

    @PrePersist
    protected void onCreate() {
        created = OffsetDateTime.now();
    }

}
