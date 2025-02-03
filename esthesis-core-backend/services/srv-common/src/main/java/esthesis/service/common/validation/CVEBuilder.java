package esthesis.service.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Constraint Violation Exception Builder provides a fluent API to build and throw custom constraint
 * violation exceptions.
 *
 * @param <D> the type of the object that is the root of the object graph
 */
public class CVEBuilder<D> {

	// Set of constraint violations.
	private final Set<ConstraintViolation<D>> violations = new HashSet<>();

	/**
	 * Helper method to create a single constraint violation.
	 *
	 * @param path    the object path of the violation.
	 * @param message the message of the violation.
	 * @return the constraint violation.
	 */
	private CVImpl<D> createViolation(String path, String message) {
		CVImpl<D> violation = new CVImpl<>();
		violation.setMessage(message);
		violation.setPropertyPath(new PathImpl(path));

		return violation;
	}

	/**
	 * Helper method to add and throw a single validation exception.
	 *
	 * @param path    the object path of the violation.
	 * @param message the message of the violation.
	 */
	public static ConstraintViolationException addAndThrow(String path, String message)
	throws ConstraintViolationException {
		return new CVEBuilder<>().add(path, message).throwCVE();
	}

	/**
	 * Helper method to add and throw a single validation exception with arguments.
	 *
	 * @param path    the object path of the violation.
	 * @param message the message of the violation.
	 * @param args    the arguments to format the message.
	 */
	public static ConstraintViolationException addAndThrow(String path, String message,
		Object... args) throws ConstraintViolationException {
		return new CVEBuilder<>().add(path, message, args).throwCVE();
	}

	/**
	 * Add a constraint violation to the builder. Use this method instead of @see addAndThrow if you
	 * plan to add multiple violations before throwing the exception.
	 *
	 * @param path    the object path of the violation.
	 * @param message the message of the violation.
	 * @return the builder instance.
	 */
	public CVEBuilder<D> add(String path, String message) {
		violations.add(createViolation(path, message));

		return this;
	}

	/**
	 * Add a constraint violation to the builder. Use this method instead of @see addAndThrow if you
	 * plan to add multiple violations before throwing the exception.
	 *
	 * @param path    the object path of the violation.
	 * @param message the message of the violation.
	 * @param args    the arguments to format the message.
	 * @return the builder instance.
	 */
	public CVEBuilder<D> add(String path, String message, Object... args) {
		violations.add(createViolation(path,
			org.slf4j.helpers.MessageFormatter.format(message, args).getMessage()));

		return this;
	}

	/**
	 * Throws a @see ConstraintViolationException, if any violations have been added to the builder.
	 */
	public ConstraintViolationException throwCVE() throws ConstraintViolationException {
//		if (!violations.isEmpty()) {
		return new ConstraintViolationException(violations);
//		}
	}

}
