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
import ph.edu.tsu.tour.web.common.function.LocationCollectionToFeatureCollection;
import ph.edu.tsu.tour.web.common.function.LocationToFeature;
import ph.edu.tsu.tour.core.location.Church;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationModifiedEvent;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.core.location.PublishingLocationService;
import ph.edu.tsu.tour.core.location.ToPublicLocationService;
import ph.edu.tsu.tour.web.Urls;

import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping(Urls.REST_V1_LOCATION)
class ChurchRestController implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(ChurchRestController.class);

    private SimpMessagingTemplate simpMessagingTemplate;
    private LocationService<Church> locationService;
    private ToPublicLocationService toPublicLocationService;

    private Function<Location, Feature> locationToFeature;
    private Function<Iterable<Location>, FeatureCollection> locationCollectionToFeatureCollection;
    private Function<LocationModifiedEvent, FeatureModifiedEvent> locationModifiedEventToFeatureModifiedEvent;

    @Autowired
    ChurchRestController(PublishingLocationService<Church> locationService,
                         ToPublicLocationService toPublicLocationService,
                         SimpMessagingTemplate simpMessagingTemplate) {
        locationService.addObserver(this);

        this.locationService = locationService;
        this.toPublicLocationService = toPublicLocationService;
        this.simpMessagingTemplate = simpMessagingTemplate;

        // TODO: Use more appropriate converters.
        this.locationToFeature = new LocationToFeature();
        this.locationCollectionToFeatureCollection = new LocationCollectionToFeatureCollection();
        this.locationModifiedEventToFeatureModifiedEvent =
                new LocationModifiedEventToFeatureModifiedEvent();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> findAll() {
        Iterable<Church> found = locationService.findAll();
        Collection<Location> modified = new HashSet<>();
        for (Church church : found) {
            toPublicLocationService.accept(church);
            modified.add(church);
        }

        FeatureCollection converted = locationCollectionToFeatureCollection.apply(modified);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Feature> findById(@PathVariable long id) {
        Church church = locationService.findById(id);
        if (church != null) {
            toPublicLocationService.accept(church);
            Feature converted = locationToFeature.apply(church);
            return ResponseEntity.ok(converted);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof LocationModifiedEvent) {
            LocationModifiedEvent locationModifiedEvent = (LocationModifiedEvent) arg;
            FeatureModifiedEvent featureModifiedEvent =
                    locationModifiedEventToFeatureModifiedEvent.apply(locationModifiedEvent);
            simpMessagingTemplate.convertAndSend("/topic/location-updates", featureModifiedEvent);
        }
    }
}
