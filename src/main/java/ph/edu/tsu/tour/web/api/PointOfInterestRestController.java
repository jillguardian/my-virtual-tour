package ph.edu.tsu.tour.web.api;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.common.function.PointOfInterestCollectionToFeatureCollection;
import ph.edu.tsu.tour.core.common.function.PointOfInterestToFeature;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PointOfInterestModifiedEvent;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.core.poi.PublishingPointOfInterestService;
import ph.edu.tsu.tour.core.poi.ToPublicPointOfInterestService;
import ph.edu.tsu.tour.web.Urls;

import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping(Urls.REST_POI)
class PointOfInterestRestController implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestRestController.class);

    private SimpMessagingTemplate simpMessagingTemplate;
    private PointOfInterestService pointOfInterestService;
    private ToPublicPointOfInterestService toPublicPointOfInterestService;

    private Function<PointOfInterest, Feature> pointOfInterestToFeature;
    private Function<Iterable<PointOfInterest>, FeatureCollection> pointOfInterestCollectionToFeatureCollection;
    private Function<PointOfInterestModifiedEvent, FeatureModifiedEvent>
            pointOfInterestModifiedEventToFeatureModifiedEvent;

    @Autowired
    PointOfInterestRestController(PublishingPointOfInterestService pointOfInterestService,
                                         ToPublicPointOfInterestService toPublicPointOfInterestService,
                                         SimpMessagingTemplate simpMessagingTemplate) {
        pointOfInterestService.addObserver(this);

        this.pointOfInterestService = pointOfInterestService;
        this.toPublicPointOfInterestService = toPublicPointOfInterestService;
        this.simpMessagingTemplate = simpMessagingTemplate;

        this.pointOfInterestToFeature = new PointOfInterestToFeature();
        this.pointOfInterestCollectionToFeatureCollection = new PointOfInterestCollectionToFeatureCollection();
        this.pointOfInterestModifiedEventToFeatureModifiedEvent =
                new PointOfInterestModifiedEventToFeatureModifiedEvent();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> findAll() {
        Iterable<PointOfInterest> found = pointOfInterestService.findAll();
        Collection<PointOfInterest> modified = new HashSet<>();
        for (PointOfInterest poi : found) {
            poi = toPublicPointOfInterestService.apply(poi);
            modified.add(poi);
        }

        FeatureCollection converted = pointOfInterestCollectionToFeatureCollection.apply(modified);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Feature> findById(@PathVariable long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi != null) {
            poi = toPublicPointOfInterestService.apply(poi);
            Feature converted = pointOfInterestToFeature.apply(poi);
            return ResponseEntity.ok(converted);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof PointOfInterestModifiedEvent) {
            PointOfInterestModifiedEvent poiModifiedEvent = (PointOfInterestModifiedEvent) arg;
            FeatureModifiedEvent featureModifiedEvent =
                    pointOfInterestModifiedEventToFeatureModifiedEvent.apply(poiModifiedEvent);
            simpMessagingTemplate.convertAndSend("/topic/poi-updates", featureModifiedEvent);
        }
    }
}
