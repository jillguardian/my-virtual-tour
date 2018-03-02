package ph.edu.tsu.tour.core.common.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Converter
public class OffsetDateTimePersistenceConverter implements
        AttributeConverter<OffsetDateTime, Timestamp> {

    @Override
    public java.sql.Timestamp convertToDatabaseColumn(OffsetDateTime entityValue) {
        return new java.sql.Timestamp(entityValue.toInstant().toEpochMilli());
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(java.sql.Timestamp databaseValue) {
        return OffsetDateTime.ofInstant(databaseValue.toInstant(), ZoneOffset.UTC);
    }
}
