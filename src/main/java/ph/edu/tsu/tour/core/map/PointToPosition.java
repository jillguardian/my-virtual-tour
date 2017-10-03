package ph.edu.tsu.tour.core.map;

import com.mapbox.services.commons.models.Position;
import org.geojson.Point;

import java.util.function.Function;

class PointToPosition implements Function<Point, Position> {
    @Override
    public Position apply(Point point) {
        if (point != null) {
            return Position.fromCoordinates(
                    point.getCoordinates().getLongitude(),
                    point.getCoordinates().getLatitude(),
                    point.getCoordinates().getAltitude());
        }
        return null;
    }
}
