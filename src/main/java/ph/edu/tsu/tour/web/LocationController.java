package ph.edu.tsu.tour.web;

import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageService;
import ph.edu.tsu.tour.core.image.Image;
import ph.edu.tsu.tour.core.image.RawImage;
import ph.edu.tsu.tour.core.image.ToPublicImageService;
import ph.edu.tsu.tour.core.location.Location;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.core.location.ToPublicLocationService;
import ph.edu.tsu.tour.exception.FunctionalityNotImplementedException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.common.dto.ImagePayload;
import ph.edu.tsu.tour.web.common.dto.LocationPayload;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// TODO: Show error messages in view
@Controller
@RequestMapping(Urls.LOCATION)
class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private LocationService locationService;
    private DiskStorageCapableImageService imageService;
    private ToPublicLocationService toPublicLocationService;
    private ToPublicImageService toPublicImageService;

    @Autowired
    LocationController(LocationService locationService,
                       DiskStorageCapableImageService diskStorageCapableImageService,
                       ToPublicLocationService toPublicLocationService,
                       ToPublicImageService toPublicImageService) throws IOException {
        this.locationService = locationService;
        this.imageService = diskStorageCapableImageService;
        this.toPublicLocationService = toPublicLocationService;
        this.toPublicImageService = toPublicImageService;
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
        Iterable<Location> locations = locationService.findAll();
        Collection<LocationPayload> dtos = new HashSet<>();
        for (Location location : locations) {
            Location publicLocation = toPublicLocationService.apply(location);
            LocationPayload dto = LocationPayload.builder()
                    .id(publicLocation.getId())
                    .name(publicLocation.getName())
                    .website(publicLocation.getWebsite())
                    .contactNumber(publicLocation.getContactNumber())
                    .addressLine1(publicLocation.getAddressLine1())
                    .addressLine2(publicLocation.getAddressLine2())
                    .city(publicLocation.getCity())
                    .zipCode(publicLocation.getZipCode())
                    .coverImage1(toDto(publicLocation, publicLocation.getCoverImage1())) // Only image displayed in view.
                    .build();
            dtos.add(dto);
        }

        model.addAttribute("locations", dtos);
        return "location/all";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String findById(@PathVariable long id, Model model) {
        Location location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException(
                    "Location with ID [" + id + "] does not exist");
        }
        if (!(location.getGeometry() instanceof Point)) {
            String type = location.getGeometry().getClass().getSimpleName();
            throw new FunctionalityNotImplementedException(
                    "Location with ID [" + id + "] has geometry of type [" + type + "]");
        }

        Location publicLocation = toPublicLocationService.apply(location);
        LocationPayload dto = LocationPayload.builder()
                .id(publicLocation.getId())
                .name(publicLocation.getName())
                .website(publicLocation.getWebsite())
                .contactNumber(publicLocation.getContactNumber())
                .addressLine1(publicLocation.getAddressLine1())
                .addressLine2(publicLocation.getAddressLine2())
                .city(publicLocation.getCity())
                .latitude(((Point) publicLocation.getGeometry()).getCoordinates().getLatitude())
                .longitude(((Point) publicLocation.getGeometry()).getCoordinates().getLongitude())
                .zipCode(publicLocation.getZipCode())
                .coverImage1(toDto(publicLocation, publicLocation.getCoverImage1()))
                .coverImage2(toDto(publicLocation, publicLocation.getCoverImage2()))
                .images(publicLocation.getImages().stream().map(raw -> toDto(publicLocation, raw)).collect(Collectors.toSet()))
                .build();

        model.addAttribute("location", dto);
        model.addAttribute("images", !location.getImages().isEmpty());
        return "location/one";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String save(Model model) {
        model.addAttribute("location", LocationPayload.builder().build());
        return "location/one";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Model model,
                       @Valid @ModelAttribute("location") LocationPayload dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            return "location/one";
        }

        Collection<Throwable> errors = new LinkedList<>();
        Location location = Location.builder()
                .id(dto.getId())
                .name(dto.getName())
                .website(dto.getWebsite())
                .contactNumber(dto.getContactNumber())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .zipCode(dto.getZipCode())
                .geometry(new Point(dto.getLongitude(), dto.getLatitude()))
                .build();
        if (dto.getId() != null) { // Set the images; we'll replace them later if necessary.
            Location existing = locationService.findById(dto.getId());
            location.setCoverImage1(existing.getCoverImage1());
            location.setCoverImage2(existing.getCoverImage2());
        }

        location = locationService.save(location);
        dto.setId(location.getId());

        final Location reference = location;

        CompletableFuture<Image> coverImage1 = null;
        CompletableFuture<Image> coverImage2 = null;
        if (!dto.getCoverImage1().getFile().isEmpty()) {
            coverImage1 = imageService.saveAsync(RawImage.builder()
                    .id(dto.getCoverImage1().getId())
                    .title(dto.getCoverImage1().getTitle())
                    .description(dto.getCoverImage1().getDescription())
                    .inputStream(dto.getCoverImage1().getFile().getInputStream())
                    .build());
        }
        if (!dto.getCoverImage2().getFile().isEmpty()) {
            coverImage2 = imageService.saveAsync(RawImage.builder()
                    .id(dto.getCoverImage2().getId())
                    .title(dto.getCoverImage2().getTitle())
                    .description(dto.getCoverImage2().getDescription())
                    .inputStream(dto.getCoverImage2().getFile().getInputStream())
                    .build());
        }

        if (coverImage1 != null) {
            try {
                location.setCoverImage1(coverImage1.get());
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
        if (coverImage2 != null) {
            try {
                location.setCoverImage2(coverImage2.get());
                logger.trace("Finished saving cover image two");
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Couldn't save cover image two", e);
                }
                if (e instanceof ExecutionException) {
                    e = (Exception) e.getCause();
                }
                errors.add(e);
            }
        }

        location = locationService.save(location);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errors", errors.stream()
                    .map(Throwable::getMessage)
                    .collect(Collectors.toList()));
        }
        return "redirect:" + Urls.LOCATION + "/" + location.getId();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(@RequestParam long id) {
        Location location = locationService.findById(id);
        if (location != null) {
            locationService.deleteById(id);

            // TODO: Hide?
            if (location.getCoverImage1() != null) {
                imageService.deleteById(location.getCoverImage1().getId());
            }
            if (location.getCoverImage2() != null) {
                imageService.deleteById(location.getCoverImage2().getId());
            }
            for (Image image : location.getImages()) {
                imageService.deleteById(image.getId());
            }
        }

        return "redirect:" + Urls.LOCATION;
    }

    @RequestMapping(value = "/{location-id}/image/new", method = RequestMethod.GET)
    public String saveImage(Model model, @PathVariable("location-id") long locationId) {
        if (!locationService.exists(locationId)) {
            throw new ResourceNotFoundException("Location with ID [" + locationId + "] does not exist");
        }
        model.addAttribute("image", ImagePayload.builder().reference(locationId).build());
        return "location/image/one";
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
        return "location/image/one";
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
            return "location/image/one";
        }

        Location location = locationService.findById(dto.getReference());
        if (location == null) {
            throw new ResourceNotFoundException(
                    "Location with ID [" + dto.getReference() + "] does not exist");
        }

        Image image;

        if (!dto.getFile().isEmpty()) {
            image = imageService.save(RawImage.builder()
                    .id(dto.getId())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .inputStream(dto.getFile().getInputStream())
                    .build());
        } else {
            image = imageService.findById(dto.getId());
            image.setTitle(dto.getTitle());
            image.setDescription(dto.getDescription());
            image = imageService.save(image);
        }

        Image temp = image; // Because Java wants an effectively final variable.

        Set<Image> images = location.getImages();
        Optional<Image> existing = images.stream().filter(x -> x.getId().equals(temp.getId())).findFirst();
        existing.ifPresent(location::removeImage);

        location.addImage(image);
        locationService.save(location);

        return "redirect:" + Urls.LOCATION + "/" + location.getId() + "/image/" + image.getId();
    }

    @RequestMapping(value = "/image/delete", method = RequestMethod.POST)
    public String deleteImage(@RequestParam("location-id") long locationId, @RequestParam("id") long imageId) {
        Location location = locationService.findById(locationId);
        if (location != null) {
            Set<Image> images = location.getImages();
            Optional<Image> match = images.stream()
                    .filter(image -> image.getId().equals(imageId))
                    .findFirst();
            if (match.isPresent()) {
                location.removeImage(match.get());
                location = locationService.save(location);
                imageService.deleteById(match.get().getId()); // TODO: Hide me.
                locationService.save(location);
            } else {
                String message = "Location [" + locationId + "] has no reference to image [" + imageId + "]";
                throw new ResourceNotFoundException(message);
            }
        }

        return "redirect:" + Urls.LOCATION + "/" + locationId + "/image";
    }

    @RequestMapping(value = "/{id}/image", method = RequestMethod.GET)
    public String findAllImages(Model model, @PathVariable long id) {
        Location location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException("Location with ID [" + id + "] does not exist");
        }

        location = toPublicLocationService.apply(location);

        Collection<Image> images = location.getImages();
        Collection<ImagePayload> dtos = new HashSet<>();
        for (Image image : images) {
            ImagePayload dto = toDto(location, image);
            dtos.add(dto);
        }

        model.addAttribute("id", id);
        model.addAttribute("images", dtos);
        return "location/image/all";
    }

}
