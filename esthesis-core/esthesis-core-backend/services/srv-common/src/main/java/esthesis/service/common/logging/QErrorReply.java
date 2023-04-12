package esthesis.service.common.logging;

import lombok.Data;

@Data
public class QErrorReply {

  private String errorMessage;
  private String traceId;
}
