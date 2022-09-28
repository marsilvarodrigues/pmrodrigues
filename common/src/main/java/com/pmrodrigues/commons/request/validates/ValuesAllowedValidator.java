package com.pmrodrigues.commons.request.validates;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

public class ValuesAllowedValidator implements ConstraintValidator<ValuesAllowed, String[]> {

    private String propName;
    private String message;
    private List<String> allowable;

    @Override
    public void initialize(ValuesAllowed requiredIfChecked) {
        this.propName = requiredIfChecked.propName();
        this.message = requiredIfChecked.message();
    }

    @Override
    public boolean isValid(String[] sortRule, ConstraintValidatorContext context) {
        var isValid = false;
        var pattern = Pattern.compile("(?![asc|desc])\\w+?.?[asc|desc]");
        for(String s : sortRule ) {
            isValid = pattern.matcher(s).find();
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(this.propName)
                    .addBeanNode()
                    .addConstraintViolation();
        }


        return isValid;

    }
}
