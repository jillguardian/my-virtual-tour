package ph.edu.tsu.tour.web.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public final class AdministratorPayload implements Serializable {

    private static final long serialVersionUID = -7155765492999713272L;

    private Long id;

    @NotNull(message = "{username.blank.message}")
    @Size(min = 1, message = "{username.blank.message}")
    private String username;

    @NotNull(message = "{password.blank.message}")
    @Size(min = 1, message = "{password.blank.message}")
    private String password;

    @NotNull(message = "{roles.blank.message}")
    @Size(min = 1, message = "{roles.blank.message}")
    @Singular
    private Collection<String> roles;

}
