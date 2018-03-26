package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.AutoPopulatingList;
import ph.edu.tsu.tour.core.location.Church.Artifact;
import ph.edu.tsu.tour.core.location.Church.Facility;
import ph.edu.tsu.tour.core.location.Church.Nearby;
import ph.edu.tsu.tour.core.location.Church.Relic;
import ph.edu.tsu.tour.core.location.Church.Style;
import ph.edu.tsu.tour.core.location.Church.Type;
import ph.edu.tsu.tour.web.common.validator.Within;
import ph.edu.tsu.tour.web.common.validator.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Within(query = "Tarlac",
        types = "region",
        countries = "PH",
        message = "{location.geometry.point.beyond-limit.message}")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(callSuper = true)
@Data
public final class ChurchPayload extends LocationPayload {

    // An ugly hack to make fields from the parent class available to this class's builder.
    @Builder(toBuilder = true)
    public ChurchPayload(Long id,
                         String name,
                         URI website,
                         String contactNumber,
                         String addressLine1,
                         String addressLine2,
                         String city,
                         String zipCode,
                         Double latitude,
                         Double longitude,
                         ImagePayload coverImage,
                         Set<ImagePayload> images,
                         Type type,
                         String saint,
                         MonthDay feastDay,
                         LocalDate canonicalErectionDay,
                         LocalDate dedicationDay,
                         String priest,
                         List<SchedulePayload> massSchedules,
                         List<SchedulePayload> confessionSchedules,
                         String massScheduleRemarks,
                         String confessionScheduleRemarks,
                         Integer visitDuration,
                         Set<Artifact> artifacts,
                         String architect,
                         Style style,
                         Year yearOfConstruction,
                         List<String> otherArchitecturalFeatures,
                         Set<Relic> relics,
                         List<String> otherRelics,
                         List<String> historicalEvents,
                         List<String> baptized,
                         List<String> married,
                         List<String> confirmed,
                         Set<Facility> facilities,
                         List<String> otherFacilities,
                         Set<Nearby> nearbies,
                         List<String> otherNearbies) {
        super(id,
              name,
              website,
              contactNumber,
              addressLine1,
              addressLine2,
              city,
              zipCode,
              latitude,
              longitude,
              coverImage,
              ChurchPayload.initialize(images));
        this.type = type;
        this.saint = saint;
        this.feastDay = feastDay;
        this.canonicalErectionDay = canonicalErectionDay;
        this.dedicationDay = dedicationDay;
        this.priest = priest;
        this.massSchedules = ChurchPayload.initialize(massSchedules, SchedulePayload.class);
        this.confessionSchedules = ChurchPayload.initialize(confessionSchedules, SchedulePayload.class);
        this.massScheduleRemarks = massScheduleRemarks;
        this.confessionScheduleRemarks = confessionScheduleRemarks;
        this.visitDuration = visitDuration;
        this.artifacts = ChurchPayload.initialize(artifacts);
        this.architect = architect;
        this.style = style;
        this.yearOfConstruction = yearOfConstruction;
        this.otherArchitecturalFeatures = ChurchPayload.initialize(otherArchitecturalFeatures, String.class);
        this.relics = ChurchPayload.initialize(relics);
        this.otherRelics = ChurchPayload.initialize(otherRelics, String.class);
        this.historicalEvents = ChurchPayload.initialize(historicalEvents, String.class);
        this.baptized = ChurchPayload.initialize(baptized, String.class);
        this.married = ChurchPayload.initialize(married, String.class);
        this.confirmed = ChurchPayload.initialize(confirmed, String.class);
        this.facilities = ChurchPayload.initialize(facilities);
        this.otherFacilities = ChurchPayload.initialize(otherFacilities, String.class);
        this.nearbies = ChurchPayload.initialize(nearbies);
        this.otherNearbies = ChurchPayload.initialize(otherNearbies, String.class);
    }

    // @NotNull(message = "{church.type.blank.message}")
    private Type type;

    // @NotNull(message = "{church.saint.blank.message}")
    private String saint;

    // @NotNull(message = "{church.feast-day.blank.message}")
    @DateTimeFormat(pattern = "MMMM dd")
    private MonthDay feastDay;

    // @NotNull(message = "{church.canonical-erection-day.blank.message}")
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate canonicalErectionDay;

    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate dedicationDay;

    // @NotNull(message = "{church.priest.blank.message}")
    private String priest;

    @NotNull(message = "{church.mass-schedules.empty.message}")
    @Size(min = 1, message = "{church.mass-schedules.empty.message}")
    private List<SchedulePayload> massSchedules = new AutoPopulatingList<>(SchedulePayload.class);

    @NotNull(message = "{church.confession-schedule.empty.message}")
    @Valid
    private List<SchedulePayload> confessionSchedules = new AutoPopulatingList<>(SchedulePayload.class);

    private String massScheduleRemarks;

    private String confessionScheduleRemarks;

    // @NotNull(message = "{church.visit-duration.blank.message}")
    private Integer visitDuration;

    private Set<Artifact> artifacts = EnumSet.noneOf(Artifact.class);

    private String architect;

    // @NotNull(message = "{church.artifacts.empty.message}")
    private Style style;

    @Range(min = 1500, max = 2018, message = "{church.year-of-construction.invalid.message}")
    Year yearOfConstruction;

    @NotNull(message = "{church.other-architectural-features.empty.message}")
    private List<String> otherArchitecturalFeatures = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.relics.empty.message}")
    private Set<Relic> relics = EnumSet.noneOf(Relic.class);

    @NotNull(message = "{church.other-relics.empty.message}")
    private List<String> otherRelics = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.historical-events.empty.message}")
    private List<String> historicalEvents = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.baptized-people.empty.message}")
    private List<String> baptized = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.married-people.empty.message}")
    private List<String> married = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.confirmed-people.empty.message}")
    private List<String> confirmed = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.facilities.empty.message}")
    private Set<Facility> facilities = EnumSet.noneOf(Facility.class);

    @NotNull(message = "{church.other-facilities.empty.message}")
    private List<String> otherFacilities = new AutoPopulatingList<>(String.class);

    @NotNull(message = "{church.nearbies.empty.message}")
    private Set<Nearby> nearbies = EnumSet.noneOf(Nearby.class);

    @NotNull(message = "{church.other-nearbies.empty.message}")
    private List<String> otherNearbies = new AutoPopulatingList<>(String.class);

    private static <E> Set<E> initialize(Set<E> set) {
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

    private static <E> List<E> initialize(List<E> list, Class<E> clazz) {
        if (list == null) {
            list = new AutoPopulatingList<>(clazz);
        }
        return list;
    }

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    @Builder(builderClassName = "Builder", toBuilder = true)
    public static final class SchedulePayload {

        public enum Language { ENGLISH, TAGALOG, KAPAMPANGAN, ILOCANO, LATIN }

        @NotNull
        private DayOfWeek day;

        @DateTimeFormat(pattern = "hh:mm a")
        @NotNull
        private LocalTime start;

        @NotNull
        private Language language;

    }

}
