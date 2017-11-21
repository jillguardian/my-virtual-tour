package ph.edu.tsu.tour.web.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class ChangePasswordPayload implements Serializable {

    private static final long serialVersionUID = -3525318407975978104L;

    @JsonProperty("token")
    private String newPasswordToken;

    @JsonProperty("password")
    private String newPassword;

}
