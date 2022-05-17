package esthesis.platform.server.nifi.client.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NiFiClientException extends RuntimeException {

  private final int httpStatusCode;
  private final String httpBody;
}
