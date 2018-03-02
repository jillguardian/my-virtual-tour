package ph.edu.tsu.tour.web.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Year;

public class YearRangeValidator implements ConstraintValidator<YearRange, Year> {

    private int min;
    private int max;

    @Override
    public void initialize(YearRange constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Year value, ConstraintValidatorContext context) {
        return value != null && (value.isBefore(Year.of(min)) || value.isAfter(Year.of(max)));
    }

}
