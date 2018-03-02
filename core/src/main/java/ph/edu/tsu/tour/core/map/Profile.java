package ph.edu.tsu.tour.core.map;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Profile {

    @JsonProperty("driving")
    DRIVING,

    @JsonProperty("cycling")
    CYCLING,

    @JsonProperty("walking")
    WALKING

}
