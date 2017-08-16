package ph.edu.tsu.tour.core.common.function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geojson.Feature;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.Map;
import java.util.function.Function;

public class PointOfInterestToFeature implements Function<PointOfInterest, Feature> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    @Override
    public Feature apply(PointOfInterest pointOfInterest) {
        Map<String, Object> properties = PointOfInterestToFeature.OBJECT_MAPPER.convertValue(
                pointOfInterest, new TypeReference<Map<String, Object>>() { });
        // FIXME: Dirty hack 'cause it's difficult to do this with Jackson's JSON Views.
        Map<String, Object> front = (Map<String, Object>) properties.get("IMAGE");
        if (front != null) {
            String uri = (String) front.get("IMAGE");
            properties.put("IMAGE", uri);
        }
        Map<String, Object> back = (Map<String, Object>) properties.get("IMAGEBACK");
        if (back != null) {
            String uri = (String) back.get("IMAGE");
            properties.put("IMAGEBACK", uri);
        }

        Feature feature = new Feature();
        feature.setGeometry(pointOfInterest.getGeometry());
        feature.setProperties(properties);
        return feature;
    }

}
