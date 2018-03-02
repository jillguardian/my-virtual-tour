package ph.edu.tsu.tour.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Actions applicable to entities.</p>
 */
public enum EntityAction {

    /**
     * Entity has been created.
     */
    @JsonProperty("created")
    CREATED,

    /**
     * Entity has been modified.
     */
    @JsonProperty("modified")
    MODIFIED,

    /**
     * Entity has been deleted.
     */
    @JsonProperty("deleted")
    DELETED

}
