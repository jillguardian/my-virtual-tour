package ph.edu.tsu.tour.core.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = User.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class User implements Serializable {

    private static final long serialVersionUID = 7580383571595167622L;
    static final String TABLE_NAME = "users";

    @Tolerate
    protected User() {
        // To make JPA happy.
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private boolean activated;

    @Data
    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public static class Payload implements Serializable {

        private static final long serialVersionUID = -5428176942386069327L;

        private String username;
        private String password;
        private String email;
    }

}
