package com.pmrodrigues.commons.request.validates;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValuesAllowedValidator.class})
public @interface ValuesAllowed {

    String message() default "Field value should be from list of ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String propName();

}
