package ph.edu.tsu.tour.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class Privilege implements Serializable {

    private static final long serialVersionUID = 5206704127027781678L;

    @Tolerate
    protected Privilege() {
        // To make JPA happy.
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

}
