package esthesis.platform.common.response;

import lombok.Data;

@Data
public abstract class GenericResponse {
  private boolean error;
  private String errorCode;
}
