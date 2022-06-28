package esthesis.common.util.validation;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

public class CVException<D> {

  private final Set<ConstraintViolation<D>> violations = new HashSet<>();

  public CVException<D> addViolation(String path, String message) {
    violations.add(
        new CVBuilder<D>().path(path).message(message).build());
    return this;
  }

  public void throwCVE() {
    throw new ConstraintViolationException(violations);
  }
}
