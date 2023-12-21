package esthesis.service.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

public class CVExceptionContainer<D> {

	private final Set<ConstraintViolation<D>> violations = new HashSet<>();

	public CVExceptionContainer<D> addViolation(String path, String message) {
		violations.add(
			new CVBuilder<D>().path(path).message(message).build());
		return this;
	}

	public CVExceptionContainer<D> addViolation(String path, String message,
		Object... args) {

		violations.add(
			new CVBuilder<D>().path(path).message(
				org.slf4j.helpers.MessageFormatter.format(message, args)
					.getMessage()
			).build());
		return this;
	}

	public void throwCVE() {
		throw new ConstraintViolationException(violations);
	}
}
