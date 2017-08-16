package ph.edu.tsu.tour.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import org.springframework.web.multipart.MultipartFile;
import ph.edu.tsu.tour.core.image.Image;
import ph.edu.tsu.tour.core.poi.PointOfInterest;
import ph.edu.tsu.tour.core.poi.PublishingPointOfInterestService;
import ph.edu.tsu.tour.core.poi.ToPublicPointOfInterestService;
import ph.edu.tsu.tour.exception.FunctionalityNotImplementedException;
import ph.edu.tsu.tour.exception.ResourceNotFoundException;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageService;
import ph.edu.tsu.tour.core.image.ToPublicImageService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: Show error messages in view
@Controller
@RequestMapping(Urls.POI)
public class PointOfInterestController {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestController.class);

    private PointOfInterestService pointOfInterestService;
    private DiskStorageCapableImageService imageService;
    private ToPublicPointOfInterestService toPublicPointOfInterestService;
    private ToPublicImageService toPublicImageService;

    @Autowired
    public PointOfInterestController(PublishingPointOfInterestService pointOfInterestService,
                                     DiskStorageCapableImageService diskStorageCapableImageService,
                                     ToPublicPointOfInterestService toPublicPointOfInterestService,
                                     ToPublicImageService toPublicImageService) throws IOException {
        this.pointOfInterestService = pointOfInterestService;
        this.imageService = diskStorageCapableImageService;
        this.toPublicPointOfInterestService = toPublicPointOfInterestService;
        this.toPublicImageService = toPublicImageService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String findAll(Model model) {
        Iterable<PointOfInterest> pois = pointOfInterestService.findAll();
        Collection<PointOfInterestDto> dtos = new HashSet<>();
        for (PointOfInterest poi : pois) {
            PointOfInterest publicPoi = toPublicPointOfInterestService.apply(poi);
            PointOfInterestDto dto = PointOfInterestDto.builder()
                    .id(publicPoi.getId())
                    .name(publicPoi.getName())
                    .website(publicPoi.getWebsite())
                    .contactNumber(publicPoi.getContactNumber())
                    .addressLine1(publicPoi.getAddressLine1())
                    .addressLine2(publicPoi.getAddressLine2())
                    .city(publicPoi.getCity())
                    .zipCode(publicPoi.getZipCode())
                    .previewImage1(toDto(publicPoi, publicPoi.getPreviewImage1())) // Only image displayed in view.
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
        PointOfInterestDto dto = PointOfInterestDto.builder()
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
                .previewImage1(toDto(publicPoi, publicPoi.getPreviewImage1()))
                .previewImage2(toDto(publicPoi, publicPoi.getPreviewImage2()))
                .images(publicPoi.getImages().stream().map(raw -> toDto(publicPoi, raw)).collect(Collectors.toSet()))
                .build();

        model.addAttribute("poi", dto);
        model.addAttribute("images", !poi.getImages().isEmpty());
        return "poi/one";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String save(Model model) {
        model.addAttribute("poi", PointOfInterestDto.builder().build());
        return "poi/one";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Model model,
                       @Valid @ModelAttribute("poi") PointOfInterestDto dto,
                       BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            return "poi/one";
        }

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
            poi.setPreviewImage1(existing.getPreviewImage1());
            poi.setPreviewImage2(existing.getPreviewImage2());
        }

        poi = pointOfInterestService.save(poi);
        dto.setId(poi.getId());

        if (!dto.getPreviewImage1().getFile().isEmpty()) {
            Image previewImage1 = imageService.save(
                    Image.builder()
                            .id(dto.getPreviewImage1().getId())
                            .title(dto.getPreviewImage1().getTitle())
                            .description(dto.getPreviewImage1().getDescription())
                            .build(),
                    dto.getPreviewImage1().getFile().getInputStream());
            if (previewImage1 == null) {
                throw new IllegalStateException("Unable to save preview image one");
            }
            poi.setPreviewImage1(previewImage1);
        }
        if (!dto.getPreviewImage2().getFile().isEmpty()) {
            Image previewImage2 = imageService.save(
                    Image.builder()
                            .id(dto.getPreviewImage2().getId())
                            .title(dto.getPreviewImage2().getTitle())
                            .description(dto.getPreviewImage2().getDescription())
                            .build(),
                    dto.getPreviewImage2().getFile().getInputStream());
            if (previewImage2 == null) {
                throw new IllegalStateException("Unable to save preview image one");
            }
            poi.setPreviewImage2(previewImage2);
        }

        poi = pointOfInterestService.save(poi);

        return "redirect:" + Urls.POI + "/" + poi.getId();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(@RequestParam long id) {
        PointOfInterest poi = pointOfInterestService.findById(id);
        if (poi != null) {
            pointOfInterestService.deleteById(id);

            // TODO: Hide?
            if (poi.getPreviewImage1() != null) {
                imageService.deleteById(poi.getPreviewImage1().getId());
            }
            if (poi.getPreviewImage2() != null) {
                imageService.deleteById(poi.getPreviewImage2().getId());
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
        model.addAttribute("image", ImageDto.builder().poiId(poiId).build());
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

        ImageDto dto = toDto(poi, image);
        model.addAttribute("image", dto);
        return "poi/image/one";
    }

    @RequestMapping(value = "/image/save", method = RequestMethod.POST)
    public String saveImage(@ModelAttribute("image") @Valid ImageDto dto,
                            BindingResult bindingResult) throws IOException {
        boolean isEmptyFile = dto.getFile() == null || dto.getFile().isEmpty();
        boolean isEmptyUri = dto.getUri() == null || dto.getUri().toString().isEmpty();
        if (isEmptyFile && isEmptyUri) {
            bindingResult.rejectValue("file", "image.file.empty", "File must be set.");
        }

        if (bindingResult.hasErrors()) {
            return "poi/image/one";
        }

        PointOfInterest poi = pointOfInterestService.findById(dto.getPoiId());
        if (poi == null) {
            throw new ResourceNotFoundException("Point of interest with ID [" + dto.getPoiId() + "] does not exist");
        }

        Image image;

        if (!dto.getFile().isEmpty()) {
            image = imageService.save(
                    Image.builder()
                            .id(dto.getId())
                            .title(dto.getTitle())
                            .description(dto.getDescription())
                            .build(),
                    dto.getFile().getInputStream());
            if (image == null) {
                throw new IllegalStateException("Unable to save image");
            }
        } else {
            image = imageService.findById(dto.getId());
            image.setTitle(dto.getTitle());
            image.setDescription(dto.getDescription());
        }

        Set<Image> images = poi.getImages();
        Optional<Image> existing = images.stream().filter(x -> x.getId().equals(image.getId())).findFirst();
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
        Collection<ImageDto> dtos = new HashSet<>();
        for (Image image : images) {
            ImageDto dto = toDto(poi, image);
            dtos.add(dto);
        }

        model.addAttribute("id", id);
        model.addAttribute("images", dtos);
        return "poi/image/all";
    }

    private static ImageDto toDto(PointOfInterest poi, Image image) {
        if (image == null || poi == null) {
            return null;
        }
        return ImageDto.builder()
                .poiId(poi.getId())
                .id(image.getId())
                .title(image.getTitle())
                .description(image.getDescription())
                .uri(image.getPreview())
                .build();
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor
    @Builder(builderClassName = "Builder", toBuilder = true)
    public static final class PointOfInterestDto {

        private Long id;
        @NotNull(message = "{poi.name.blank.message}")
        @Size(min = 1, message = "{poi.name.blank.message}")
        private String name;
        private URI website;
        private String contactNumber;
        private String addressLine1;
        private String addressLine2;
        @NotNull(message = "{poi.city.blank.message}")
        @Size(min = 1, message = "{poi.city.blank.message}")
        private String city;
        @NotNull(message = "{poi.zip.blank.message}")
        @Size(min = 1, message = "{poi.zip.blank.message}")
        private String zipCode;
        @NotNull(message = "{poi.geometry.point.latitude.blank.message}")
        private Double latitude;
        @NotNull(message = "{poi.geometry.point.longitude.blank.message}")
        private Double longitude;
        private ImageDto previewImage1;
        private ImageDto previewImage2;
        private Set<ImageDto> images;

    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor
    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public static final class ImageDto {

        private Long poiId;
        private Long id;
        private String title;
        private String description;
        private MultipartFile file;
        private URI uri;

    }

}
