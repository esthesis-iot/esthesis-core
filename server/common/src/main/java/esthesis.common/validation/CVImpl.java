package esthesis.common.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import lombok.Data;

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
  public ConstraintDescriptor<?> constraintDescriptor;

  @Override
  public <U> U unwrap(Class<U> type) {
    return null;
  }
}
