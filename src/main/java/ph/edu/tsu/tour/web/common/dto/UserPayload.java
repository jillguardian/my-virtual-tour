package ph.edu.tsu.tour.web.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class UserPayload implements Serializable {

    private static final long serialVersionUID = -5428176942386069327L;

    private String username;
    private String password;
    private String email;

}
