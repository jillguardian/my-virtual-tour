package ph.edu.tsu.tour.web.common.validator;

import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.exception.FailedDependencyException;
import ph.edu.tsu.tour.web.common.dto.LocationPayload;
import retrofit2.Response;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;

public class WithinLocationValidator implements ConstraintValidator<Within, LocationPayload> {

    private static final String DEFAULT_APPLICATION_NAME = Project.getName() + "/" + Project.getVersion();
    private static final Logger logger = LoggerFactory.getLogger(WithinLocationValidator.class);

    private final String applicationName;
    private final String accessToken;

    private String place;
    private String[] types;
    private String[] countries;

    // TODO: Remove ugly Spring-related qualifier. Keep classes like this Spring-free!
    public WithinLocationValidator(@Value("${application.map.mapbox.access-token}") String accessToken) {
        this.applicationName = WithinLocationValidator.DEFAULT_APPLICATION_NAME;
        this.accessToken = accessToken;
    }

    @Override
    public void initialize(Within annotation) {
        this.place = annotation.query();
        this.types = annotation.types();
        this.countries = annotation.countries();
    }

    @Override
    public boolean isValid(LocationPayload value, ConstraintValidatorContext context) {
        Double longitude = value.getLongitude();
        Double latitude = value.getLatitude();

        if (longitude == null || latitude == null) {
            return true;
        }

        MapboxGeocoding getEnclosingArea = new MapboxGeocoding.Builder<>()
                .setAccessToken(accessToken)
                .setClientAppName(applicationName)
                .setLocation(place)
                .setCountries(countries)
                .setGeocodingTypes(types)
                .setLimit(1)
                .build();

        String enclosingAreaId;
        try {
            Response<GeocodingResponse> response = getEnclosingArea.executeCall();
            if (response.isSuccessful() && response.body().getFeatures().get(0).getId() != null) {
                enclosingAreaId = response.body().getFeatures().get(0).getId();
            } else {
                throw new FailedDependencyException("Unsuccessful response [" + response.code() + "]");
            }
        } catch (Exception e) {
            throw new FailedDependencyException("Couldn't get data on [" + place + "]", e);
        }

        // Now that we have the ID of the enclosing area, we'll check if the location in the payload belongs to the
        // enclosing area.

        MapboxGeocoding getArea = new MapboxGeocoding.Builder<>()
                .setAccessToken(accessToken)
                .setClientAppName(applicationName)
                .setLocation(longitude + "," + latitude)
                .setLimit(1)
                .build();

        try {
            Response<GeocodingResponse> response = getArea.executeCall();
            if (response.isSuccessful()) {
                CarmenFeature carmenFeature = response.body().getFeatures().get(0);
                return carmenFeature.getContext().stream().anyMatch(c -> c.getId().equals(enclosingAreaId));
            } else {
                throw new FailedDependencyException("Unsuccessful response [" + response.code() + "]");
            }
        } catch (Exception e) {
            throw new FailedDependencyException("Couldn't get data on [" + longitude + ", " + latitude + "]", e);
        }

    }

}
