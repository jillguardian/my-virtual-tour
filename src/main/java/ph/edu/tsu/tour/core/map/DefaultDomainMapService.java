package ph.edu.tsu.tour.core.map;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.springframework.http.ResponseEntity;
import ph.edu.tsu.tour.core.poi.PointOfInterest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultDomainMapService implements DomainMapService {

    private MapService mapService;

    public DefaultDomainMapService(MapService mapService) {
        this.mapService = mapService;
    }

    @Override
    public PointOfInterest findNearest(Profile profile, GeoJsonObject source, List<PointOfInterest> destinations) {
        List<GeoJsonObject> destinationCoordinates = destinations.stream()
                .map(PointOfInterest::getGeometry)
                .collect(Collectors.toList());
        int index = mapService.findNearest(profile, source, destinationCoordinates);
        if (index > 0) {
            return destinations.get(index);
        }
        return null;
    }

}
