package ph.edu.tsu.tour.core.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import ph.edu.tsu.tour.core.common.converter.UriPersistenceConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Entity
@Table(name = Image.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Image implements Serializable {

    private static final long serialVersionUID = 5606095850624355737L;
    static final String TABLE_NAME = "images";

    @Tolerate
    protected Image() {

    }

    @Id
    @GeneratedValue
    @JsonProperty("ID")
    private Long id;

    @JsonProperty("TITLE")
    private String title;

    @Column(length = 5000)
    @JsonProperty("DESC")
    private String description;

    @Column(nullable = false, unique = false, length = 2000)
    @Convert(converter = UriPersistenceConverter.class)
    @JsonProperty("IMAGE")
    private URI location;

    @Column(length = 2000)
    @JsonProperty("THUMBNAIL")
    @Convert(converter = UriPersistenceConverter.class)
    private URI preview;

    private Integer priority;

    @ElementCollection
    private Set<String> tags = new HashSet<>();

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    public void addTag(String tag) {
        if (tags.contains(tag)) {
            return;
        }
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Image image) {
        return Image.builder()
                .id(image.getId())
                .title(image.getTitle())
                .description(image.getDescription())
                .location(image.getPreview())
                .preview(image.getPreview())
                .priority(image.getPriority())
                .tags(image.getTags());
    }

}
