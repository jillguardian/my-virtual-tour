package ph.edu.tsu.tour.core.map;

import com.mapbox.services.api.directions.v5.DirectionsCriteria;

import java.util.function.Function;

/**
 * <p>Used for converting application-specific {@link Profile} to Mapbox's equivalent profile values.</p>
 */
class ProfileToProfile implements Function<Profile, String> {

    @Override
    public String apply(Profile profile) {
        switch (profile) {
            case CYCLING:
                return DirectionsCriteria.PROFILE_CYCLING;
            case DRIVING:
                return DirectionsCriteria.PROFILE_DRIVING;
            case WALKING:
                return DirectionsCriteria.PROFILE_WALKING;
        }
        throw new IllegalArgumentException("Unknown profile [" + profile + "]");
    }

}
