package esthesis.common.device.commands;

import esthesis.common.device.dto.BaseDTO;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommandReplyDTO extends BaseDTO  {
  // The command id for which this reply is for.
  @NotEmpty
  private long commandRequestId;

  // The payload of the reply.
  @NotEmpty
  private String payload;

  // A string indicating the type of the payload.
  private String payloadType;

  // A string indicating the encoding of the payload.
  private String payloadEncoding;

}
