package test.com.pmrodrigues.commons.request.validates;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.commons.request.validates.ValuesAllowedValidator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValuesAllowedValidatorTest {

    @Test
    void isValid() {
        var allowedValidator = new ValuesAllowedValidator();
        val isValid = allowedValidator.isValid(new String[]{"field.desc"}, null);

        assertTrue(isValid);
    }

    @Test
    void isValidOnlyFieldName() {
        var allowedValidator = new ValuesAllowedValidator();
        val isValid = allowedValidator.isValid(new String[]{"field"}, null);

        assertTrue(isValid);
    }

    @Test
    void isInvalidOnlyDirection() {

        var validAllowed = mock(ValuesAllowed.class);
        var context = mock(ConstraintValidatorContext.class);
        var constraintBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(validAllowed.propName()).thenReturn("sort");
        when(validAllowed.message()).thenReturn("sort is invalid");
        when(context.buildConstraintViolationWithTemplate(any(String.class))).thenReturn(constraintBuilder);
        when(constraintBuilder.addPropertyNode(any(String.class))).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);


        var allowedValidator = new ValuesAllowedValidator();
        allowedValidator.initialize(validAllowed);
        var isValid = allowedValidator.isValid(new String[]{"desc"}, context);

        assertFalse(isValid);
        isValid = allowedValidator.isValid(new String[]{"asc"}, context);
        assertFalse(isValid);
    }
}