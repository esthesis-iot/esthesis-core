package esthesis.platform.backend.server.nifi.client.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NiFiClientException extends RuntimeException {

  private final int httpStatusCode;
  private final String httpBody;
}
