package esthesis.service.crypto.impl.resource;

import lombok.Data;

@Data
public class QErrorReply {

  private String errorMessage;
  private String traceId;
}
