package ph.edu.tsu.tour.web.common.function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geojson.Feature;
import ph.edu.tsu.tour.core.location.Location;

import java.util.function.Function;

public class FeatureToLocation implements Function<Feature, Location> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    @Override
    public Location apply(Feature feature) {
        Location location = FeatureToLocation.OBJECT_MAPPER.convertValue(
                feature.getProperties(), Location.class);
        location.setGeometry(feature.getGeometry());
        return location;
    }

}
