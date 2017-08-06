package ph.edu.tsu.tour.domain;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class PointsOfInterest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    private PointsOfInterest() {
        throw new AssertionError("Intentionally unimplemented");
    }

    public static PointOfInterest convert(Feature feature) {
        PointOfInterest pointOfInterest = PointsOfInterest.OBJECT_MAPPER.convertValue(
                feature.getProperties(), PointOfInterest.class);
        pointOfInterest.setGeometry(feature.getGeometry());
        return pointOfInterest;
    }

    public static Collection<PointOfInterest> convert(FeatureCollection featureCollection) {
        Collection<PointOfInterest> pointOfInterests = new ArrayList<>();
        for (Feature feature : featureCollection.getFeatures()) {
            pointOfInterests.add(PointsOfInterest.convert(feature));
        }
        return pointOfInterests;
    }

    public static Feature convert(PointOfInterest pointOfInterest) {
        Map<String, Object> properties = PointsOfInterest.OBJECT_MAPPER.convertValue(
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

    public static FeatureCollection convert(Iterable<PointOfInterest> pointsOfInterest) {
        FeatureCollection features = new FeatureCollection();
        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            features.add(convert(pointOfInterest));
        }
        return features;
    }

}
