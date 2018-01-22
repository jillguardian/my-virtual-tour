package ph.edu.tsu.tour.core.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Administrator implements Serializable {

    private static final long serialVersionUID = -931930487819800097L;

    @Tolerate
    protected Administrator() {
        // To make JPA happy.
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @JsonProperty("first-name")
    @Column(nullable = false)
    private String firstName;

    @JsonProperty("last-name")
    @Column
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles;

}
