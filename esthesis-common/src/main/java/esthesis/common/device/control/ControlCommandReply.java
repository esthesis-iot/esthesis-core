package esthesis.common.device.control;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ControlCommandReply {
  // The command id for which this reply is for.
  @NotEmpty
  private String commandRequestId;

  // The payload of the reply.
  @NotEmpty
  private String payload;

  // A string indicating the type of the payload.
  private String payloadType;

  // A string indicating the encoding of the payload.
  private String payloadEncoding;
}
