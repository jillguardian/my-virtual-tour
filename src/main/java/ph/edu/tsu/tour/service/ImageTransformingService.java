package ph.edu.tsu.tour.service;

import ph.edu.tsu.tour.domain.Image;

import java.util.function.Function;

public interface ImageTransformingService extends Function<Image, Image> {
}
