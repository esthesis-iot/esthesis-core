package esthesis.platform.server.nifi.client.exception;

import lombok.Getter;

public class NiFiProcessingException extends RuntimeException {

  @Getter
  private int httpCode;

  public NiFiProcessingException(String message) {
    super(message);
  }

  public NiFiProcessingException(String message, int httpCode) {
    super(message);
    this.httpCode = httpCode;
  }

}
