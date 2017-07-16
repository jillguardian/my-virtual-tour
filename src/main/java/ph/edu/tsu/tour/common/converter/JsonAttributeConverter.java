package ph.edu.tsu.tour.common.converter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public abstract class JsonAttributeConverter<X> implements AttributeConverter<X, String> {

    private static final Logger logger = LoggerFactory.getLogger(JsonAttributeConverter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    private final Class<? extends X> actualClass;
    private final TypeReference<? extends X> typeReference;

    private JsonAttributeConverter(Class<? extends X> actualClass, TypeReference<? extends X> typeReference) {
        if (typeReference != null ^ actualClass != null) {
            this.typeReference = typeReference;
            this.actualClass = actualClass;
        } else {
            throw new IllegalArgumentException("Must set [typeReference] or [actualClass]");
        }
    }

    protected JsonAttributeConverter(Class<? extends X> actualClass) {
        this(actualClass, null);
    }

    /**
     * Recommended for attributes with generic types.
     */
    protected JsonAttributeConverter(TypeReference<? extends X> typeReference) {
        this(null, typeReference);
    }

    @Override
    public String convertToDatabaseColumn(X attribute) {
        String converted = "";
        if (attribute != null) {
            try {
                converted = OBJECT_MAPPER.writeValueAsString(attribute);
            } catch (JsonProcessingException ex) {
                throw Throwables.propagate(ex);
            }
        }
        return converted;
    }

    @Override
    public X convertToEntityAttribute(String dbData) {
        X converted = null;
        if (dbData != null) {
            try {
                if (actualClass != null) {
                    converted = OBJECT_MAPPER.readValue(dbData, actualClass);
                } else if (typeReference != null) {
                    converted = OBJECT_MAPPER.readValue(dbData, typeReference);
                } else {
                    throw new AssertionError("Neither [typeReference] nor [actualClass] was set");
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to convert [" + dbData + "]", e);
                }
            }
        }
        return converted;
    }

}