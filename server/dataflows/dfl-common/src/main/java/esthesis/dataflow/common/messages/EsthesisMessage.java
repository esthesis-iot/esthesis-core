package esthesis.dataflow.common.messages;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EsthesisMessage {

  public enum MessageType {
    PING,
    TELEMETRY,
    METADATA,
    CONTROL_REQUEST,
    CONTROL_REPLY
  }

  // A UUID uniquely identifying this message.
  public String id;

  // Indicates the correlation of this message to another message.
  public String correlationId;

  // The hardware ID generated this message.
  public String hardwareId;

  // The type of this message.
  public MessageType type;

  // The channel on which this message was received. The name of the channel
  // is specific to the medium by which the message was received.
  public String channel;

  // The payload of this message.
  public String payload;

}
