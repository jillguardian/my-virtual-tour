package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ph.edu.tsu.tour.web.Urls;
import ph.edu.tsu.tour.domain.PointOfInterest;
import ph.edu.tsu.tour.domain.PointsOfInterest;
import ph.edu.tsu.tour.service.PointOfInterestService;

@RestController("restPointOfInterestController")
@RequestMapping(Urls.REST_POI)
public class PointOfInterestController {

    private PointOfInterestService pointOfInterestService;

    @Autowired
    public PointOfInterestController(PointOfInterestService pointOfInterestService) {
        this.pointOfInterestService = pointOfInterestService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> findAll() {
        Iterable<PointOfInterest> found = pointOfInterestService.findAll();
        FeatureCollection converted = PointsOfInterest.convert(found);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Feature> findById(@PathVariable long id) {
        PointOfInterest found = pointOfInterestService.findById(id);
        return ResponseEntity.ok(PointsOfInterest.convert(found));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Feature> save(@RequestBody Feature feature) {
        PointOfInterest converted = PointsOfInterest.convert(feature);
        PointOfInterest saved = pointOfInterestService.save(converted);
        return ResponseEntity.ok(PointsOfInterest.convert(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Feature> deleteById(@PathVariable long id) {
        PointOfInterest found = pointOfInterestService.findById(id);
        if (found != null) {
            boolean deleted = pointOfInterestService.deleteById(id);
            if (deleted) {
                return ResponseEntity.ok(PointsOfInterest.convert(found));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
