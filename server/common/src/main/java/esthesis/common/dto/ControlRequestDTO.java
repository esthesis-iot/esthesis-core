package esthesis.common.dto;

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
public class ControlRequestDTO {

  // An id uniquely identifying this message. This id is used to correlate
  // the response to this message, usually by specifying the 'correlationId'
  // property in the reply inside EsthesisMessage.
  String id;

  // The hardware id of the recipient of this message.
  String hardwareId;

  // The command to execute.
  String command;

  // The arguments to be provided together with the command.
  String args;

  // A description about the control request message.
  String description;
}
