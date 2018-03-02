package ph.edu.tsu.tour.runtime.mvc.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class MonthDayConditionalConverter implements GenericConverter, ConditionalConverter {

    /**
     * @return {@code true} if {@link MonthDay} is annotated with {@link DateTimeFormat}
     */
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Predicate<TypeDescriptor> predicate = descriptor ->
                descriptor.getType() == MonthDay.class && descriptor.getAnnotation(DateTimeFormat.class) != null;
        return predicate.test(sourceType) || predicate.test(targetType);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(String.class, MonthDay.class));
        pairs.add(new ConvertiblePair(MonthDay.class, String.class));
        return pairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType() == String.class && targetType.getType() == MonthDay.class) {
            String string = String.class.cast(source);
            if (string == null || string.isEmpty()) {
                return null;
            }
            DateTimeFormat format = targetType.getAnnotation(DateTimeFormat.class);
            return MonthDay.parse(string, DateTimeFormatter.ofPattern(format.pattern()));
        } else if (sourceType.getType() == MonthDay.class && targetType.getType() == String.class) {
            MonthDay monthDay = MonthDay.class.cast(source);
            if (monthDay == null) {
                return "";
            }
            DateTimeFormat format = sourceType.getAnnotation(DateTimeFormat.class);
            return monthDay.format(DateTimeFormatter.ofPattern(format.pattern()));
        } else {
            throw new UnsupportedOperationException(
                    "Could not convert [" + sourceType.getName() + "] to [" + targetType.getName() + "]");
        }
    }
}
