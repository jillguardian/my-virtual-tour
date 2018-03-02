package ph.edu.tsu.tour.web.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class ChangePasswordPayload implements Serializable {

    private static final long serialVersionUID = -3525318407975978104L;

    @NotNull(message = "{token.blank.message}")
    @Size(min = 1, message = "{token.blank.message}")
    @JsonProperty("token")
    private String newPasswordToken;

    @NotNull(message = "{password.blank.message}")
    @Size(min = 1, message = "{password.blank.message}")
    @JsonProperty("password")
    private String newPassword;

}
