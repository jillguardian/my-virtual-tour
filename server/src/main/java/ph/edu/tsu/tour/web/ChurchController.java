package ph.edu.tsu.tour.web;

import org.apache.commons.lang3.StringUtils;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AutoPopulatingList;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.Valid;

import ph.edu.tsu.tour.core.image.DiskStorageCapableImageService;
import ph.edu.tsu.tour.core.image.Image;
import ph.edu.tsu.tour.core.image.RawImage;
import ph.edu.tsu.tour.core.image.ToPublicImageService;
import ph.edu.tsu.tour.core.location.Church;
import ph.edu.tsu.tour.core.location.Church.Schedule;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.core.location.ToPublicLocationService;
import ph.edu.tsu.tour.exception.FunctionalityNotImplementedException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.common.dto.ChurchPayload;
import ph.edu.tsu.tour.web.common.dto.ImagePayload;

@Controller
@RequestMapping(Urls.CHURCH_LOCATION)
class ChurchController {

    private static final Logger logger = LoggerFactory.getLogger(ChurchController.class);

    private LocationService<Church> locationService;
    private DiskStorageCapableImageService imageService;
    private ToPublicLocationService toPublicLocationService;
    private ToPublicImageService toPublicImageService;

    @Autowired
    ChurchController(LocationService<Church> locationService,
                     DiskStorageCapableImageService diskStorageCapableImageService,
                     ToPublicLocationService toPublicLocationService,
                     ToPublicImageService toPublicImageService) throws IOException {
        this.locationService = locationService;
        this.imageService = diskStorageCapableImageService;
        this.toPublicLocationService = toPublicLocationService;
        this.toPublicImageService = toPublicImageService;
    }

    private static Church toEntity(ChurchPayload dto) {
        Predicate<String> isNullOrEmptyString = StringUtils::isBlank;
        Predicate<ChurchPayload.SchedulePayload> isNullOrEmptySchedule = Objects::isNull; // TODO: Blank or null fields.
        dto.getMassSchedules().removeIf(isNullOrEmptySchedule);
        dto.getConfessionSchedules().removeIf(isNullOrEmptySchedule);
        dto.getOtherArchitecturalFeatures().removeIf(isNullOrEmptyString);
        dto.getHistoricalEvents().removeIf(isNullOrEmptyString);
        dto.getBaptized().removeIf(isNullOrEmptyString);
        dto.getMarried().removeIf(isNullOrEmptyString);
        dto.getConfirmed().removeIf(isNullOrEmptyString);
        dto.getOtherFacilities().removeIf(isNullOrEmptyString);
        dto.getOtherNearbies().removeIf(isNullOrEmptyString);

        Church church = Church.builder()
                .id(dto.getId())
                .name(dto.getName())
                .website(dto.getWebsite())
                .contactNumber(dto.getContactNumber())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .zipCode(dto.getZipCode())
                .geometry(new Point(dto.getLongitude(), dto.getLatitude()))
                .type(dto.getType())
                .saint(dto.getSaint())
                .feastDay(dto.getFeastDay())
                .canonicalErectionDay(dto.getCanonicalErectionDay())
                .dedicationDay(dto.getDedicationDay())
                .priest(dto.getPriest())
                .massSchedules(
                        dto.getMassSchedules().stream()
                                .map(payload -> Schedule.builder()
                                        .day(payload.getDay())
                                        .start(payload.getStart())
                                        .end(payload.getEnd())
                                        .language(payload.getLanguage().name())
                                        .build())
                                .collect(Collectors.toSet()))
                .confessionSchedules(
                        dto.getConfessionSchedules().stream()
                                .map(payload -> Schedule.builder()
                                        .day(payload.getDay())
                                        .start(payload.getStart())
                                        .end(payload.getEnd())
                                        .language(payload.getLanguage().name())
                                        .build())
                                .collect(Collectors.toSet()))
                .artifacts(dto.getArtifacts())
                .architect(dto.getArchitect())
                .style(dto.getStyle())
                .yearOfConstruction(dto.getYearOfConstruction())
                .otherArchitecturalFeatures(new HashSet<>(dto.getOtherArchitecturalFeatures()))
                .relics(dto.getRelics())
                .historicalEvents(new HashSet<>(dto.getHistoricalEvents()))
                .baptized(new HashSet<>(dto.getBaptized()))
                .married(new HashSet<>(dto.getMarried()))
                .confirmed(new HashSet<>(dto.getConfirmed()))
                .facilities(dto.getFacilities())
                .otherFacilities(new HashSet<>(dto.getOtherFacilities()))
                .nearbies(dto.getNearbies())
                .otherNearbies(new HashSet<>(dto.getOtherNearbies()))
                .build();
        return church;
    }

    private static ChurchPayload toDto(Church church) {
        ChurchPayload dto = ChurchPayload.builder()
                .id(church.getId())
                .name(church.getName())
                .website(church.getWebsite())
                .contactNumber(church.getContactNumber())
                .addressLine1(church.getAddressLine1())
                .addressLine2(church.getAddressLine2())
                .city(church.getCity())
                .latitude(((Point) church.getGeometry()).getCoordinates().getLatitude())
                .longitude(((Point) church.getGeometry()).getCoordinates().getLongitude())
                .zipCode(church.getZipCode())
                .coverImage(toDto(church, church.getCoverImage()))
                .images(church.getImages().stream().map(raw -> toDto(church, raw)).collect(Collectors.toSet()))
                .type(church.getType())
                .saint(church.getSaint())
                .feastDay(church.getFeastDay())
                .canonicalErectionDay(church.getCanonicalErectionDay())
                .dedicationDay(church.getDedicationDay())
                .priest(church.getPriest())
                .massSchedules(new AutoPopulatingList<>(
                        church.getMassSchedules().stream()
                                .map(ChurchController::toDto)
                                .collect(Collectors.toList()),
                        ChurchPayload.SchedulePayload.class))
                .confessionSchedules(new AutoPopulatingList<>(
                        church.getConfessionSchedules().stream()
                                .map(ChurchController::toDto)
                                .collect(Collectors.toList()),
                        ChurchPayload.SchedulePayload.class))
                .artifacts(church.getArtifacts())
                .architect(church.getArchitect())
                .style(church.getStyle())
                .yearOfConstruction(church.getYearOfConstruction())
                .otherArchitecturalFeatures(new ArrayList<>(church.getOtherArchitecturalFeatures()))
                .relics(church.getRelics())
                .historicalEvents(new ArrayList<>(church.getHistoricalEvents()))
                .baptized(new ArrayList<>(church.getBaptized()))
                .married(new ArrayList<>(church.getMarried()))
                .confirmed(new ArrayList<>(church.getConfirmed()))
                .facilities(church.getFacilities())
                .otherFacilities(new ArrayList<>(church.getOtherFacilities()))
                .nearbies(church.getNearbies())
                .otherNearbies(new ArrayList<>(church.getOtherNearbies()))
                .build();
        return dto;
    }

    private static ChurchPayload.SchedulePayload toDto(Schedule schedule) {
        return ChurchPayload.SchedulePayload.builder()
                .day(schedule.getDay())
                .start(schedule.getStart())
                .end(schedule.getEnd())
                .language(ChurchPayload.SchedulePayload.Language.valueOf(schedule.getLanguage()))
                .build();
    }

    private static ImagePayload toDto(Location location, Image image) {
        if (image == null || location == null) {
            return null;
        }
        return ImagePayload.builder()
                .reference(location.getId())
                .id(image.getId())
                .title(image.getTitle())
                .description(image.getDescription())
                .uri(image.getPreview())
                .build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String findAll(Model model) {
        Iterable<Church> locations = locationService.findAll();
        Collection<ChurchPayload> dtos = new HashSet<>();
        for (Church location : locations) {
            toPublicLocationService.accept(location);
            // TODO: Reduce payload properties to only what is required.
            ChurchPayload dto = ChurchPayload.builder()
                    .id(location.getId())
                    .name(location.getName())
                    .website(location.getWebsite())
                    .contactNumber(location.getContactNumber())
                    .addressLine1(location.getAddressLine1())
                    .addressLine2(location.getAddressLine2())
                    .city(location.getCity())
                    .zipCode(location.getZipCode())
                    .coverImage(toDto(location, location.getCoverImage())) // Only image displayed in view.
                    .build();
            dtos.add(dto);
        }

        model.addAttribute("locations", dtos);
        return "church/all";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String findById(@PathVariable long id, Model model) {
        Church church = locationService.findById(id);
        if (church == null) {
            throw new ResourceNotFoundException(
                    "Location with ID [" + id + "] does not exist");
        }
        if (!(church.getGeometry() instanceof Point)) {
            String type = church.getGeometry().getClass().getSimpleName();
            throw new FunctionalityNotImplementedException(
                    "Church with ID [" + id + "] has geometry of type [" + type + "]");
        }

        toPublicLocationService.accept(church);
        ChurchPayload dto = toDto(church);

        model.addAttribute("location", dto);
        model.addAttribute("images", !church.getImages().isEmpty());
        return "church/one";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String save(Model model) {
        model.addAttribute("location", ChurchPayload.builder().build());
        return "church/one";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Locale locale,
                       Model model,
                       @Valid @ModelAttribute("location") ChurchPayload dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {
        // TODO: Prefer annotation-based validation.
        // Can't do this with annotations at the moment because JPA does not support collection-based validation.
        performExtraValidation(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            return "church/one";
        }

        Collection<Throwable> errors = new LinkedList<>();
        Church church = toEntity(dto);

        if (dto.getId() != null) { // Set the images; we'll replace them later if necessary.
            Church existing = locationService.findById(dto.getId());
            church.setCoverImage(existing.getCoverImage());
        }

        church = locationService.save(church);
        dto.setId(church.getId());

        CompletableFuture<Image> coverImage = null;
        if (!dto.getCoverImage().getFile().isEmpty()) {
            coverImage = imageService.saveAsync(RawImage.builder()
                    .id(dto.getCoverImage().getId())
                    .title(dto.getCoverImage().getTitle())
                    .description(dto.getCoverImage().getDescription())
                    .inputStream(dto.getCoverImage().getFile().getInputStream())
                    .build());
        }

        if (coverImage != null) {
            try {
                church.setCoverImage(coverImage.get());
                logger.trace("Finished saving cover image one");
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Couldn't save cover image one", e);
                }
                if (e instanceof ExecutionException) {
                    e = (Exception) e.getCause();
                }
                errors.add(e);
            }
        }

        church = locationService.save(church);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errors", errors.stream()
                    .map(Throwable::getMessage)
                    .collect(Collectors.toList()));
        }
        return "redirect:" + Urls.CHURCH_LOCATION + "/" + church.getId();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(@RequestParam long id) {
        Location location = locationService.findById(id);
        if (location != null) {
            // TODO: Hide?
            if (location.getCoverImage() != null) {
                location.setCoverImage(null);
                imageService.deleteById(location.getCoverImage().getId());
            }
            for (Image image : location.getImages()) {
                location.removeImage(image);
                imageService.deleteById(image.getId());
            }

            locationService.deleteById(id);
        }

        return "redirect:" + Urls.CHURCH_LOCATION;
    }

    @RequestMapping(value = "/{location-id}/image/new", method = RequestMethod.GET)
    public String saveImage(Model model, @PathVariable("location-id") long locationId) {
        if (!locationService.exists(locationId)) {
            throw new ResourceNotFoundException("Location with ID [" + locationId + "] does not exist");
        }
        model.addAttribute("image", ImagePayload.builder().reference(locationId).build());
        return "church/image/one";
    }

    @RequestMapping(value = "/{location-id}/image/{id}", method = RequestMethod.GET)
    public String findImageById(Model model,
                                @PathVariable("location-id") long locationId,
                                @PathVariable("id") long imageId) throws MalformedURLException {
        Location location = locationService.findById(locationId);
        if (location == null) {
            throw new ResourceNotFoundException("Location with ID [" + locationId + "] does not exist");
        }

        Image image = imageService.findById(imageId);
        image = toPublicImageService.apply(image);
        if (image == null) {
            throw new ResourceNotFoundException("Image with ID [" + imageId + "] does not exist");
        }

        ImagePayload dto = toDto(location, image);
        model.addAttribute("image", dto);
        return "church/image/one";
    }

    @RequestMapping(value = "/image/save", method = RequestMethod.POST)
    public String saveImage(@ModelAttribute("image") @Valid ImagePayload dto,
                            BindingResult bindingResult) throws IOException {
        boolean isEmptyFile = dto.getFile() == null || dto.getFile().isEmpty();
        boolean isEmptyUri = dto.getUri() == null || dto.getUri().toString().isEmpty();
        if (isEmptyFile && isEmptyUri) {
            bindingResult.rejectValue("file", "image.file.empty", "File must be set.");
        }

        if (bindingResult.hasErrors()) {
            return "church/image/one";
        }

        Church church = locationService.findById(dto.getReference());
        if (church == null) {
            throw new ResourceNotFoundException(
                    "Location with ID [" + dto.getReference() + "] does not exist");
        }

        Image image;

        boolean exists = dto.getId() != null;
        if (!dto.getFile().isEmpty()) { // Updating the actual image file?
            image = imageService.save(RawImage.builder()
                    .id(dto.getId())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .inputStream(dto.getFile().getInputStream())
                    .build());
        } else { // Updating fields only?
            image = imageService.findById(dto.getId());
            image.setTitle(dto.getTitle());
            image.setDescription(dto.getDescription());
            image = imageService.save(image);
        }

        if (!exists) {
            church.addImage(image);
        }

        locationService.save(church);

        return "redirect:" + Urls.CHURCH_LOCATION + "/" + church.getId() + "/image/" + image.getId();
    }

    @RequestMapping(value = "/image/delete", method = RequestMethod.POST)
    public String deleteImage(@RequestParam("location-id") long locationId, @RequestParam("id") long imageId) {
        Church church = locationService.findById(locationId);
        if (church != null) {
            Set<Image> images = church.getImages();
            Optional<Image> match = images.stream()
                    .filter(image -> image.getId().equals(imageId))
                    .findFirst();
            if (match.isPresent()) {
                church.removeImage(match.get());
                church = locationService.save(church);
                imageService.deleteById(match.get().getId()); // TODO: Hide me.
                locationService.save(church);
            } else {
                String message = "Location [" + locationId + "] has no reference to image [" + imageId + "]";
                throw new ResourceNotFoundException(message);
            }
        }

        return "redirect:" + Urls.CHURCH_LOCATION + "/" + locationId + "/image";
    }

    @RequestMapping(value = "/{id}/image", method = RequestMethod.GET)
    public String findAllImages(Model model, @PathVariable long id) {
        Location location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException("Location with ID [" + id + "] does not exist");
        }

        toPublicLocationService.accept(location);

        Collection<Image> images = location.getImages();
        Collection<ImagePayload> dtos = new HashSet<>();
        for (Image image : images) {
            ImagePayload dto = toDto(location, image);
            dtos.add(dto);
        }

        model.addAttribute("id", id);
        model.addAttribute("images", dtos);
        return "church/image/all";
    }

    private void performExtraValidation(ChurchPayload church, BindingResult bindingResult) {
        if (church.getCanonicalErectionDay() != null && church.getCanonicalErectionDay().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("canonicalErectionDay",
                                      "church.canonical-erection-day.invalid.message",
                                      "Selected date is a future date!");
        }
        if (church.getDedicationDay() != null && church.getDedicationDay().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("dedicationDay",
                                      "church.dedication-day.invalid.message",
                                      "Selected date is a future date!");
        }
        removeNullStringsAndRejectEmptyStrings(church.getOtherArchitecturalFeatures(),
                                               bindingResult,
                                               "otherArchitecturalFeatures",
                                               "church.other-architectural-feature.empty.message",
                                               "Feature can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getOtherRelics(),
                                               bindingResult,
                                               "otherRelics",
                                               "church.other-relic.empty.message",
                                               "Relic can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getHistoricalEvents(),
                                               bindingResult,
                                               "historicalEvents",
                                               "church.historical-event.empty.message",
                                               "Historical event can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getBaptized(),
                                               bindingResult,
                                               "baptized",
                                               "church.baptized-person.empty.message",
                                               "Name of baptized person can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getMarried(),
                                               bindingResult,
                                               "married",
                                               "church.married-person.empty.message",
                                               "Name of married person can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getConfirmed(),
                                               bindingResult,
                                               "confirmed",
                                               "church.confirmed-person.empty.message",
                                               "Name of confirmed person can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getOtherFacilities(),
                                               bindingResult,
                                               "otherFacilities",
                                               "church.other-facility.empty.message",
                                               "Facility can't be blank.");
        removeNullStringsAndRejectEmptyStrings(church.getOtherNearbies(),
                                               bindingResult,
                                               "otherNearbies",
                                               "church.other-nearby.empty.message",
                                               "Place can't be blank.");
        removeNullAndRejectInvalidSchedules(church.getMassSchedules(), bindingResult, "massSchedules");
        removeNullAndRejectInvalidSchedules(church.getConfessionSchedules(), bindingResult, "confessionSchedules");
    }

    private void removeNullStringsAndRejectEmptyStrings(List<String> strings,
                                                        BindingResult bindingResult,
                                                        String propertyName,
                                                        String errorCode,
                                                        String defaultErrorMessage) {
        strings.removeIf(Objects::isNull);
        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            if (string.trim().isEmpty()) {
                bindingResult.rejectValue(propertyName + "[" + i + "]", errorCode, defaultErrorMessage);
                if (!bindingResult.hasFieldErrors(propertyName)) {
                    bindingResult.rejectValue(propertyName, "errors-found", "There are errors.");
                }
            }
        }
    }

    private void removeNullAndRejectInvalidSchedules(List<ChurchPayload.SchedulePayload> schedules,
                                                     BindingResult bindingResult,
                                                     String propertyName) {
        schedules.removeIf(Objects::isNull);
        for (int i = 0; i < schedules.size(); i++) {
            boolean error = false;
            ChurchPayload.SchedulePayload schedule = schedules.get(i);
            if (schedule.getDay() == null) {
                bindingResult.rejectValue(propertyName + "[" + i + "].day",
                                          "church.schedule.day.empty.message",
                                          "Day should be specified.");
            }
            if (schedule.getStart() == null) {
                bindingResult.rejectValue(propertyName + "[" + i + "].start",
                                          "church.schedule.start.empty.message",
                                          "Day should be specified.");
            }
            if (schedule.getEnd() == null) {
                bindingResult.rejectValue(propertyName + "[" + i + "].end",
                                          "church.schedule.end.empty.message",
                                          "Day should be specified.");
            }
            if (schedule.getStart() != null && schedule.getEnd() != null 
                    && (schedule.getStart().isAfter(schedule.getEnd())
                    || schedule.getEnd().equals(schedule.getStart()))) {
                bindingResult.rejectValue(propertyName + "[" + i + "].start",
                                          "church.schedule.start.invalid.message",
                                          "Ending time should come after starting time.");
                error = true;
            }
            if (schedule.getLanguage() == null) {
                bindingResult.rejectValue(propertyName + "[" + i + "].language",
                                          "church.schedule.language.empty.message",
                                          "Language should be specified.");
                error = true;
            }
        }
    }

}
