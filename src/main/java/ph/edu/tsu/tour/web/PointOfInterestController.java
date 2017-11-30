package ph.edu.tsu.tour.web;

import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.core.poi.ToPublicPointOfInterestService;
import ph.edu.tsu.tour.exception.FunctionalityNotImplementedException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.web.common.dto.ImagePayload;
import ph.edu.tsu.tour.web.common.dto.PointOfInterestPayload;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
@RequestMapping(Urls.POI)
class PointOfInterestController {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestController.class);

    private PointOfInterestService pointOfInterestService;
    private DiskStorageCapableImageService imageService;
    private ToPublicPointOfInterestService toPublicPointOfInterestService;
    private ToPublicImageService toPublicImageService;

    @Autowired
    PointOfInterestController(PointOfInterestService pointOfInterestService,
                              DiskStorageCapableImageService diskStorageCapableImageService,
                              ToPublicPointOfInterestService toPublicPointOfInterestService,
                              ToPublicImageService toPublicImageService) throws IOException {
        this.pointOfInterestService = pointOfInterestService;
        this.imageService = diskStorageCapableImageService;
        this.toPublicPointOfInterestService = toPublicPointOfInterestService;
        this.toPublicImageService = toPublicImageService;
    }

    private static ImagePayload toDto(PointOfInterest poi, Image image) {
        if (image == null || poi == null) {
            return null;
        }
        return ImagePayload.builder()
                .reference(poi.getId())
                .id(image.getId())
                .title(image.getTitle())
                .description(image.getDescription())
                .uri(image.getPreview())
                .build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String findAll(Model model) {
        Iterable<PointOfInterest> pois = pointOfInterestService.findAll();
        Collection<PointOfInterestPayload> dtos = new HashSet<>();
        for (PointOfInterest poi : pois) {
            PointOfInterest publicPoi = toPublicPointOfInterestService.apply(poi);
            PointOfInterestPayload dto = PointOfInterestPayload.builder()
                    .id(publicPoi.getId())
                    .name(publicPoi.getName())
                    .website(publicPoi.getWebsite())
                    .contactNumber(publicPoi.getContactNumber())
                    .addressLine1(publicPoi.getAddressLine1())
                    .addressLine2(publicPoi.getAddressLine2())
                    .city(publicPoi.getCity())
                    .zipCode(publicPoi.getZipCode())
                    .coverImage1(toDto(publicPoi, publicPoi.getCoverImage1())) // Only image displayed in view.
                    .build();
            dtos.add(dto);
        }

        model.addAttribute("pois", dtos);
        return "poi/all";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String findById(@PathVariable long id, Model model) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi == null) {
            throw new ResourceNotFoundException(
                    "Point of interest with ID [" + id + "] does not exist");
        }
        if (!(poi.getGeometry() instanceof Point)) {
            String type = poi.getGeometry().getClass().getSimpleName();
            throw new FunctionalityNotImplementedException(
                    "Point of interest with ID [" + id + "] has geometry of type [" + type + "]");
        }

        PointOfInterest publicPoi = toPublicPointOfInterestService.apply(poi);
        PointOfInterestPayload dto = PointOfInterestPayload.builder()
                .id(publicPoi.getId())
                .name(publicPoi.getName())
                .website(publicPoi.getWebsite())
                .contactNumber(publicPoi.getContactNumber())
                .addressLine1(publicPoi.getAddressLine1())
                .addressLine2(publicPoi.getAddressLine2())
                .city(publicPoi.getCity())
                .latitude(((Point) publicPoi.getGeometry()).getCoordinates().getLatitude())
                .longitude(((Point) publicPoi.getGeometry()).getCoordinates().getLongitude())
                .zipCode(publicPoi.getZipCode())
                .coverImage1(toDto(publicPoi, publicPoi.getCoverImage1()))
                .coverImage2(toDto(publicPoi, publicPoi.getCoverImage2()))
                .images(publicPoi.getImages().stream().map(raw -> toDto(publicPoi, raw)).collect(Collectors.toSet()))
                .build();

        model.addAttribute("poi", dto);
        model.addAttribute("images", !poi.getImages().isEmpty());
        return "poi/one";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String save(Model model) {
        model.addAttribute("poi", PointOfInterestPayload.builder().build());
        return "poi/one";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Model model,
                       @Valid @ModelAttribute("poi") PointOfInterestPayload dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            return "poi/one";
        }

        Collection<Throwable> errors = new LinkedList<>();
        PointOfInterest poi = PointOfInterest.builder()
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
            PointOfInterest existing = pointOfInterestService.findById(dto.getId());
            poi.setCoverImage1(existing.getCoverImage1());
            poi.setCoverImage2(existing.getCoverImage2());
        }

        poi = pointOfInterestService.save(poi);
        dto.setId(poi.getId());

        final PointOfInterest reference = poi;

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
                poi.setCoverImage1(coverImage1.get());
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
                poi.setCoverImage2(coverImage2.get());
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

        poi = pointOfInterestService.save(poi);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errors", errors.stream()
                    .map(Throwable::getMessage)
                    .collect(Collectors.toList()));
        }
        return "redirect:" + Urls.POI + "/" + poi.getId();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(@RequestParam long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi != null) {
            pointOfInterestService.deleteById(id);

            // TODO: Hide?
            if (poi.getCoverImage1() != null) {
                imageService.deleteById(poi.getCoverImage1().getId());
            }
            if (poi.getCoverImage2() != null) {
                imageService.deleteById(poi.getCoverImage2().getId());
            }
            for (Image image : poi.getImages()) {
                imageService.deleteById(image.getId());
            }
        }

        return "redirect:" + Urls.POI;
    }

    @RequestMapping(value = "/{poiId}/image/new", method = RequestMethod.GET)
    public String saveImage(Model model, @PathVariable("poiId") long poiId) {
        if (!pointOfInterestService.exists(poiId)) {
            throw new ResourceNotFoundException("Point of interest with ID [" + poiId + "] does not exist");
        }
        model.addAttribute("image", ImagePayload.builder().reference(poiId).build());
        return "poi/image/one";
    }

    @RequestMapping(value = "/{poiId}/image/{id}", method = RequestMethod.GET)
    public String findImageById(Model model,
                                @PathVariable long poiId,
                                @PathVariable("id") long imageId) throws MalformedURLException {
        PointOfInterest poi = pointOfInterestService.findById(poiId);
        if (poi == null) {
            throw new ResourceNotFoundException("Point of interest with ID [" + poiId + "] does not exist");
        }

        Image image = imageService.findById(imageId);
        image = toPublicImageService.apply(image);
        if (image == null) {
            throw new ResourceNotFoundException("Image with ID [" + imageId + "] does not exist");
        }

        ImagePayload dto = toDto(poi, image);
        model.addAttribute("image", dto);
        return "poi/image/one";
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
            return "poi/image/one";
        }

        PointOfInterest poi = pointOfInterestService.findById(dto.getReference());
        if (poi == null) {
            throw new ResourceNotFoundException(
                    "Point of interest with ID [" + dto.getReference() + "] does not exist");
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

        Set<Image> images = poi.getImages();
        Optional<Image> existing = images.stream().filter(x -> x.getId().equals(temp.getId())).findFirst();
        existing.ifPresent(poi::removeImage);

        poi.addImage(image);
        pointOfInterestService.save(poi);

        return "redirect:" + Urls.POI + "/" + poi.getId() + "/image/" + image.getId();
    }

    @RequestMapping(value = "/image/delete", method = RequestMethod.POST)
    public String deleteImage(@RequestParam("poi-id") long poiId, @RequestParam("id") long imageId) {
        PointOfInterest poi = pointOfInterestService.findById(poiId);
        if (poi != null) {
            Set<Image> images = poi.getImages();
            Optional<Image> match = images.stream()
                    .filter(image -> image.getId().equals(imageId))
                    .findFirst();
            if (match.isPresent()) {
                poi.removeImage(match.get());
                imageService.deleteById(match.get().getId()); // TODO: Hide me.
                pointOfInterestService.save(poi);
            } else {
                String message = "Point of interest [" + poiId + "] has no reference to image [" + imageId + "]";
                throw new ResourceNotFoundException(message);
            }
        }

        return "redirect:" + Urls.POI + "/" + poiId + "/image";
    }

    @RequestMapping(value = "/{id}/image", method = RequestMethod.GET)
    public String findAllImages(Model model, @PathVariable long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi == null) {
            throw new ResourceNotFoundException("Point of interest with ID [" + id + "] does not exist");
        }

        poi = toPublicPointOfInterestService.apply(poi);

        Collection<Image> images = poi.getImages();
        Collection<ImagePayload> dtos = new HashSet<>();
        for (Image image : images) {
            ImagePayload dto = toDto(poi, image);
            dtos.add(dto);
        }

        model.addAttribute("id", id);
        model.addAttribute("images", dtos);
        return "poi/image/all";
    }

}
