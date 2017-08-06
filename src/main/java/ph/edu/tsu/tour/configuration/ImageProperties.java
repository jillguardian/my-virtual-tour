package ph.edu.tsu.tour.configuration;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Getter
@EqualsAndHashCode
final class ImageProperties {

    private Wrapper main;
    private Wrapper preview;

    @Autowired
    public ImageProperties(@Value("${poi.image.resize}") boolean autoResize,
                           @Value("${poi.image.max-width}") Integer maxWidth,
                           @Value("${poi.image.max-height}") Integer maxHeight,
                           @Value("${poi.image.quality}") Double quality,
                           @Value("${poi.image.preview.resize}") boolean previewAutoResize,
                           @Value("${poi.image.preview.max-width}") Integer previewMaxWidth,
                           @Value("${poi.image.preview.max-height}") Integer previewMaxHeight,
                           @Value("${poi.image.preview.quality}") Double previewQuality) {
        Wrapper.Builder mainBuilder = Wrapper.builder();
        Wrapper.Builder previewBuilder = Wrapper.builder();

        if (autoResize) {
            Objects.requireNonNull(maxWidth, "Must set max width");
            Objects.requireNonNull(maxHeight, "Must set max height");
            if (maxHeight < 1 || maxWidth < 1) {
                throw new IllegalArgumentException("Image dimensions must be greater than zero");
            }
            mainBuilder
                    .autoResize(true)
                    .maxWidth(maxWidth)
                    .maxHeight(maxHeight);
        }
        if (quality != null) {
            if (quality < 0 || quality > 1) {
                throw new IllegalArgumentException("Image quality must be a number between 0 and 1");
            }
            mainBuilder.quality(quality);
        }
        if (previewAutoResize) {
            Objects.requireNonNull(previewMaxWidth, "Must set preview max width");
            Objects.requireNonNull(previewMaxHeight, "Must set preview max height");
            if (previewMaxHeight < 1 || previewMaxWidth < 1) {
                throw new IllegalArgumentException("Preview image dimensions must be greater than zero");
            }
            previewBuilder
                    .autoResize(true)
                    .maxWidth(previewMaxWidth)
                    .maxHeight(previewMaxHeight);
        }
        if (previewQuality != null) {
            if (previewQuality < 0 || previewQuality > 1) {
                throw new IllegalArgumentException("Preview image quality must be a number between 0 and 1");
            }
            previewBuilder.quality(previewQuality);
        }
        this.main = mainBuilder.build();
        this.preview = previewBuilder.build();
    }

    @Data
    @Builder(builderClassName = "Builder", toBuilder = true)
    static final class Wrapper {

        private boolean autoResize;
        private int maxWidth;
        private int maxHeight;
        private double quality;

        private static class Builder {

            private boolean autoResize = false;
            private int maxWidth = -1;
            private int maxHeight = -1;
            private double quality = 1;

        }

    }

}