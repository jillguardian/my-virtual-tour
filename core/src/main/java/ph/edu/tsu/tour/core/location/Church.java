package ph.edu.tsu.tour.core.location;

import java.io.Serializable;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetTime;
import java.time.Year;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;
import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ph.edu.tsu.tour.core.image.Image;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Entity
@Table(name = Church.TABLE_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@EqualsAndHashCode(callSuper = true)
public class Church extends Location implements Serializable {

    private static final long serialVersionUID = -3630097319659354235L;
    static final String TABLE_NAME = "churches";

    public enum Type { PARISH, SHRINE, CHAPEL }
    public enum Style { BAROQUE, GOTHIC, NEOCLASSIC, MODERN }
    public enum Artifact { MURAL_PAINTING, OIL_PAINTING, CANVAS_PAINTING, STATUE, LITURGICAL_VESSEL, BELL }
    public enum Relic { FIRST_CLASS, SECOND_CLASS }
    public enum Facility {
        COMFORT_ROOM,
        GREEN_PARK,
        PARKING_LOT,
        PWD_ACCESSIBILITY,
        PARISH_HALL,
        PILGRIM_CENTER,
        PARISH_MUSEUM,
        SOUVENIR_SHOP
    }
    public enum Nearby {
        RESTAURANT,
        HOSPITAL,
        PHARMACY,
        GASOLINE_STATION
    }

    // An ugly hack to make fields from the parent class available to this class's builder.
    @Builder(toBuilder = true)
    public Church(Long id,
                  String name,
                  URI website,
                  String contactNumber,
                  Image coverImage,
                  Set<Image> images,
                  String addressLine1,
                  String addressLine2,
                  String city,
                  String zipCode,
                  GeoJsonObject geometry,
                  Type type,
                  String saint,
                  MonthDay feastDay,
                  LocalDate canonicalErectionDay,
                  LocalDate dedicationDay,
                  String priest,
                  Set<Schedule> massSchedules,
                  Set<Schedule> confessionSchedules,
                  String massScheduleRemarks,
                  String confessionScheduleRemarks,
                  Integer visitDuration,
                  Set<Artifact> artifacts,
                  String architect,
                  Style style,
                  Year yearOfConstruction,
                  Set<String> otherArchitecturalFeatures,
                  Set<Relic> relics,
                  Set<String> otherRelics,
                  Set<String> historicalEvents,
                  Set<String> baptized,
                  Set<String> married,
                  Set<String> confirmed,
                  Set<Facility> facilities,
                  Set<String> otherFacilities,
                  Set<Nearby> nearbies,
                  Set<String> otherNearbies) {
        super( id,
               name,
               website,
               contactNumber,
               coverImage,
               Church.initialize(images), // Manually initialize because Lombok doesn't.
               addressLine1,
               addressLine2,
               city,
               zipCode,
               geometry );
        this.type = type;
        this.saint = saint;
        this.feastDay = feastDay;
        this.canonicalErectionDay = canonicalErectionDay;
        this.dedicationDay = dedicationDay;
        this.priest = priest;
        this.massSchedules = massSchedules;
        this.confessionSchedules = confessionSchedules;
        this.massScheduleRemarks = massScheduleRemarks;
        this.confessionScheduleRemarks = confessionScheduleRemarks;
        this.visitDuration = visitDuration;
        this.artifacts = artifacts;
        this.architect = architect;
        this.style = style;
        this.yearOfConstruction = yearOfConstruction;
        this.otherArchitecturalFeatures = otherArchitecturalFeatures;
        this.relics = relics;
        this.otherRelics = otherRelics;
        this.historicalEvents = historicalEvents;
        this.baptized = baptized;
        this.married = married;
        this.confirmed = confirmed;
        this.facilities = facilities;
        this.otherFacilities = otherFacilities;
        this.nearbies = nearbies;
        this.otherNearbies = otherNearbies;
    }

    @Column(nullable = true)
    private Type type;

    @Column(nullable = true)
    private String saint;

    @JsonProperty("feast-day")
    @JsonFormat(pattern = "MM/dd")
    @Column(name = "feast_day", nullable = true)
    private MonthDay feastDay;

    @JsonProperty("canonical-erection-day")
    @JsonFormat(pattern = "MM/dd/yyyy")
    @Column(name = "canonical_erection_day", nullable = true)
    private LocalDate canonicalErectionDay;

    @JsonProperty("dedication-day")
    @JsonFormat(pattern = "MM/dd/yyyy")
    @Column(name = "dedication_day")
    private LocalDate dedicationDay;

    @Column(nullable = true)
    private String priest;

    @JsonProperty("mass-schedules")
    @ElementCollection()
    @CollectionTable(name = "mass_schedules", joinColumns = @JoinColumn(name = "church_id"))
    private Set<Schedule> massSchedules = new HashSet<>();

    @JsonProperty("confession-schedules")
    @ElementCollection()
    @CollectionTable(name = "confession_schedules", joinColumns = @JoinColumn(name = "church_id"))
    private Set<Schedule> confessionSchedules = new HashSet<>();

    @JsonProperty("mass-schedule-remarks")
    @Column(length = 1000)
    private String massScheduleRemarks;

    @JsonProperty("confession-schedule-remarks")
    @Column(length = 1000)
    private String confessionScheduleRemarks;

    @JsonProperty("duration-of-visit")
    @Column(name = "duration_of_visit")
    private Integer visitDuration;

    @ElementCollection()
    private Set<Artifact> artifacts = EnumSet.noneOf( Artifact.class );

    private String architect;
    private Style style;

    @JsonProperty("year-of-construction")
    @Column(name = "year_of_construction")
    private Year yearOfConstruction;

    @JsonProperty("other-architectural-features")
    @ElementCollection()
    private Set<String> otherArchitecturalFeatures = new HashSet<>();

    @ElementCollection()
    private Set<Relic> relics = EnumSet.noneOf( Relic.class );

    @JsonProperty("other-relics")
    @ElementCollection()
    private Set<String> otherRelics = new HashSet<>();

    @JsonProperty("historical-events")
    @ElementCollection()
    private Set<String> historicalEvents = new HashSet<>();

    @ElementCollection()
    private Set<String> baptized = new HashSet<>();

    @ElementCollection()
    private Set<String> married = new HashSet<>();

    @ElementCollection()
    private Set<String> confirmed = new HashSet<>();

    @ElementCollection()
    private Set<Facility> facilities = EnumSet.noneOf( Facility.class );

    @JsonProperty("other-facilities")
    @Column(name = "other_facilities")
    @ElementCollection()
    private Set<String> otherFacilities = new HashSet<>();

    @JsonProperty("nearby-places")
    @ElementCollection()
    private Set<Nearby> nearbies = EnumSet.noneOf( Nearby.class );

    @JsonProperty("other-nearby-places")
    @Column(name = "other_nearbies")
    @ElementCollection()
    private Set<String> otherNearbies = new HashSet<>();

    // TODO: Use zoned time fields.
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Data
    @Builder(builderClassName = "Builder", toBuilder = true)
    @Embeddable
    public static class Schedule {

        @Tolerate
        protected Schedule() {
            // JPA.
        }

        private DayOfWeek day;

        @JsonFormat(pattern = "hh:mm a")
        private LocalTime start;

        private String language;

    }

    private static <E> Set<E> initialize(Set<E> set) {
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

}
