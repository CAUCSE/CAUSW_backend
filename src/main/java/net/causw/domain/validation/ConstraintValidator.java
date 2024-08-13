package net.causw.domain.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;

public class ConstraintValidator <T>{
    public void validate(T object, Validator validator) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            violations.forEach( violation ->
                    sb.append(violation.getMessage())
            );

            throw new ConstraintViolationException(sb.toString(), violations);
        }
    }
}
