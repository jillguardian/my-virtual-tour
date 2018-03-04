package ph.edu.tsu.tour.web.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Year;

public class YearRangeValidator implements ConstraintValidator<Range, Year> {

    private int min;
    private int max;

    @Override
    public void initialize(Range constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Year value, ConstraintValidatorContext context) {
        return value == null || (value.isAfter(Year.of(min)) && value.isBefore(Year.of(max)));
    }

}
