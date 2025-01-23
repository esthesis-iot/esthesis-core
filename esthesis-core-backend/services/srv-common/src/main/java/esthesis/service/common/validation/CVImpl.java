package esthesis.service.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.Data;

/**
 * Implementation of the @{@link ConstraintViolation} interface.
 *
 * @param <D> the type of the object that is the root of the object graph.
 */
@Data
public class CVImpl<D> implements ConstraintViolation<D> {

	private String message;
	private String messageTemplate;
	private D rootBean;
	private Class<D> rootBeanClass;
	private Object leafBean;
	private Object[] executableParameters;
	private Object executableReturnValue;
	private Path propertyPath;
	private Object invalidValue;
	private ConstraintDescriptor<?> constraintDescriptor;

	@Override
	public <U> U unwrap(Class<U> type) {
		return null;
	}
}
