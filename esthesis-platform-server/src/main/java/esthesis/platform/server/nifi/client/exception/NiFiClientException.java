package esthesis.platform.server.nifi.client.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NiFiClientException extends RuntimeException {

  private int httpStatusCode;
  private String httpBody;
}
