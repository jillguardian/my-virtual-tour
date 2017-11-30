package ph.edu.tsu.tour.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.core.tour.Tour;
import ph.edu.tsu.tour.core.tour.TourService;
import ph.edu.tsu.tour.core.user.User;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.exception.IllegalArgumentException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.exception.UnauthorizedAccessException;
import ph.edu.tsu.tour.web.Urls;
import ph.edu.tsu.tour.web.common.dto.TourPayload;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Urls.REST_V1_TOUR)
public class TourRestController {

    private final TourService tourService;
    private final LocationService locationService;
    private final UserService userService;

    @Autowired
    TourRestController(TourService tourService,
                       LocationService locationService,
                       UserService userService) {
        this.tourService = tourService;
        this.locationService = locationService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<TourPayload>> findAll(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        Iterable<Tour> tours = tourService.findByAuthor(user);

        Collection<TourPayload> convertedTours = new HashSet<>();
        for (Tour tour : tours) {
            TourPayload convertedTour = TourPayload.builder()
                    .id(tour.getId())
                    .title(tour.getTitle())
                    .description(tour.getDescription())
                    .locations(tour.getLocations().stream().map(Location::getId).collect(Collectors.toSet()))
                    .build();
            convertedTours.add(convertedTour);
        }

        return ResponseEntity.ok(convertedTours);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<TourPayload> save(Authentication authentication,
                                            @Valid @RequestBody TourPayload payload,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Found [" + bindingResult.getErrorCount() + "] errors in payload");
        }

        User user = userService.findByUsername(authentication.getName());
        Tour tour = Tour.builder()
                .id(payload.getId())
                .author(user)
                .title(payload.getTitle())
                .description(payload.getDescription())
                .locations(payload.getLocations().stream().map(locationService::findById).collect(Collectors.toSet()))
                .build();

        tour = tourService.save(tour);
        TourPayload saved = TourPayload.builder()
                .id(tour.getId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .locations(tour.getLocations().stream().map(Location::getId).collect(Collectors.toSet()))
                .build();
        return ResponseEntity.ok(saved);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(Authentication authentication, long id) {
        User user = userService.findByUsername(authentication.getName());
        Tour tour = tourService.findById(id);
        if (tour == null) {
            throw new ResourceNotFoundException("Tour [" + id + "] does not exist");
        }

        if (!tour.getAuthor().equals(user)) {
            throw new UnauthorizedAccessException("Tour [" + id + "] is restricted");
        }

        tourService.deleteById(id);
        return ResponseEntity.accepted().build();
    }

}
