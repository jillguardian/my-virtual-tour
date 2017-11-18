package ph.edu.tsu.tour.core.user;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import ph.edu.tsu.tour.core.common.converter.OffsetDateTimePersistenceConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Table(name = VerificationToken.TABLE_NAME)
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class NewPasswordToken implements Serializable {

    private static final long serialVersionUID = -771511756475438805L;
    static final String TABLE_NAME = "new_password_tokens";

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String content;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimePersistenceConverter.class)
    private OffsetDateTime created;

    @Tolerate
    protected NewPasswordToken() {
        // To make JPA happy.
    }

    @Tolerate
    protected NewPasswordToken(User user, String content) {
        this.user = user;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        created = OffsetDateTime.now();
    }

}
