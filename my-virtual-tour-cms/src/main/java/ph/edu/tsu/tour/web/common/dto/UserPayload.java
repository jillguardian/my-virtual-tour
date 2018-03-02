package ph.edu.tsu.tour.web.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class UserPayload implements Serializable {

    private static final long serialVersionUID = -5428176942386069327L;

    @NotNull(message = "{username.blank.message}")
    @Size(min = 1, message = "{username.blank.message}")
    private String username;

    @NotNull(message = "{password.blank.message}")
    @Size(min = 1, message = "{password.blank.message}")
    private String password;

    @NotNull(message = "{email.blank.message}")
    @Size(min = 1, message = "{email.blank.message}")
    private String email;

}
