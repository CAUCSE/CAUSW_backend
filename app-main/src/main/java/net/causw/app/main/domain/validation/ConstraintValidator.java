package net.causw.app.main.domain.validation;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ConstraintValidator<T> extends AbstractValidator {

	private final T domainModel;

	private final Validator validator;

	private ConstraintValidator(T domainModel, Validator validator) {
		this.domainModel = domainModel;
		this.validator = validator;
	}

	public static <T> ConstraintValidator<T> of(T domainModel, Validator validator) {
		return new ConstraintValidator<T>(domainModel, validator);
	}

	@Override
	public void validate() {
		Set<ConstraintViolation<T>> violations = this.validator.validate(this.domainModel);

		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			violations.forEach(violation ->
				sb.append(violation.getMessage())
			);

			throw new ConstraintViolationException(sb.toString(), violations);
		}
	}
}
