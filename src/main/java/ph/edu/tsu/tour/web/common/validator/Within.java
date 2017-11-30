package ph.edu.tsu.tour.web.common.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = WithinLocationValidator.class)
public @interface Within {
    String query();
    String country();
    String message();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
