package ph.edu.tsu.tour.common.converter;

import org.geojson.GeoJsonObject;

import javax.persistence.Converter;

@Converter
public class GeoJsonObjectConverter extends JsonAttributeConverter<GeoJsonObject> {

    public GeoJsonObjectConverter() {
        super(GeoJsonObject.class);
    }

}
