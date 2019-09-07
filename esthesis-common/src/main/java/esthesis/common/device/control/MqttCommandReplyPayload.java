package esthesis.common.device.control;

import lombok.Data;

@Data
public class MqttCommandReplyPayload {
  // The command id for which this reply is for.
  private long commandId;
  private byte[] payload;
}
