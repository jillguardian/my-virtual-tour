package ph.edu.tsu.tour.common.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;
import java.net.URISyntaxException;

@Converter
public class UriPersistenceConverter implements AttributeConverter<URI, String> {

    private static final Logger logger = LoggerFactory.getLogger(UriPersistenceConverter.class);

    @Override
    public String convertToDatabaseColumn(URI attribute) {
        if (attribute != null) {
            return attribute.toString();
        }
        return null;
    }

    @Override
    public URI convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            try {
                return new URI(dbData);
            } catch (URISyntaxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to convert [" + dbData + "]", e);
                }
            }
        }
        return null;
    }

}
