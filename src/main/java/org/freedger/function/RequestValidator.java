package org.freedger.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

public class RequestValidator {
    private final Validator validator;

    public RequestValidator() {
        this(Validation.buildDefaultValidatorFactory().getValidator());
    }

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }
    
    public <T> void validate(T object) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<T> violation : violations) {
                errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
            }
            throw new ValidationException(String.join("\n", errors));
        }
    }
}
