package ph.edu.tsu.tour.core.common.function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geojson.Feature;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.function.Function;

public class FeatureToPointOfInterest implements Function<Feature, PointOfInterest> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    @Override
    public PointOfInterest apply(Feature feature) {
        PointOfInterest pointOfInterest = FeatureToPointOfInterest.OBJECT_MAPPER.convertValue(
                feature.getProperties(), PointOfInterest.class);
        pointOfInterest.setGeometry(feature.getGeometry());
        return pointOfInterest;
    }

}
