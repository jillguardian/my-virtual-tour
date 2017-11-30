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
import ph.edu.tsu.tour.core.common.function.LocationCollectionToFeatureCollection;
import ph.edu.tsu.tour.core.common.function.LocationToFeature;
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
@RequestMapping(Urls.REST_LOCATION)
class LocationRestController implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(LocationRestController.class);

    private SimpMessagingTemplate simpMessagingTemplate;
    private LocationService locationService;
    private ToPublicLocationService toPublicLocationService;

    private Function<Location, Feature> locationToFeature;
    private Function<Iterable<Location>, FeatureCollection> locationCollectionToFeatureCollection;
    private Function<LocationModifiedEvent, FeatureModifiedEvent> locationModifiedEventToFeatureModifiedEvent;

    @Autowired
    LocationRestController(PublishingLocationService locationService,
                           ToPublicLocationService toPublicLocationService,
                           SimpMessagingTemplate simpMessagingTemplate) {
        locationService.addObserver(this);

        this.locationService = locationService;
        this.toPublicLocationService = toPublicLocationService;
        this.simpMessagingTemplate = simpMessagingTemplate;

        this.locationToFeature = new LocationToFeature();
        this.locationCollectionToFeatureCollection = new LocationCollectionToFeatureCollection();
        this.locationModifiedEventToFeatureModifiedEvent =
                new LocationModifiedEventToFeatureModifiedEvent();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> findAll() {
        Iterable<Location> found = locationService.findAll();
        Collection<Location> modified = new HashSet<>();
        for (Location location : found) {
            location = toPublicLocationService.apply(location);
            modified.add(location);
        }

        FeatureCollection converted = locationCollectionToFeatureCollection.apply(modified);
        return ResponseEntity.ok(converted);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Feature> findById(@PathVariable long id) {
        Location location = locationService.findById(id);
        if (location != null) {
            location = toPublicLocationService.apply(location);
            Feature converted = locationToFeature.apply(location);
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
