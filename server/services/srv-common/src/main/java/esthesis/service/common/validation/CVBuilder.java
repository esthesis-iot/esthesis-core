package esthesis.service.common.validation;

import lombok.Data;

@Data
public class CVBuilder<D> {

  private String path;
  private String message;

  public CVBuilder<D> path(String path) {
    this.path = path;
    return this;
  }

  public CVBuilder<D> message(String message) {
    this.message = message;
    return this;
  }

  public CVImpl<D> build() {
    CVImpl<D> violation = new CVImpl<>();
    violation.setMessage(message);
    violation.setPropertyPath(new PathImpl(path));

    return violation;
  }
}
