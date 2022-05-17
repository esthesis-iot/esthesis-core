package esthesis.platform.server.nifi.client.exception;

import lombok.Getter;

public class NiFiProcessingException extends RuntimeException {

  @Getter
  private final int httpCode;

  public NiFiProcessingException(String message, int httpCode) {
    super(message);
    this.httpCode = httpCode;
  }

}
