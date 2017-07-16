package ph.edu.tsu.tour.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Role implements Serializable {

    private static final long serialVersionUID = 5284537197612248547L;

    @Tolerate
    protected Role() {
        // To make JPA happy.
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Privilege> privileges;

}
