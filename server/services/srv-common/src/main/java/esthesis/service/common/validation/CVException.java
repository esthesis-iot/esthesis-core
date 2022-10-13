package esthesis.service.common.validation;

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

  public CVException<D> addViolation(String path, String message,
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
