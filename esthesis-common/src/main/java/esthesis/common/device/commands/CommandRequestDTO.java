package esthesis.common.device.commands;

import esthesis.common.device.dto.BaseDTO;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CommandRequestDTO extends BaseDTO {

  // The operation this command is about (for example, PING, EXECUTE, etc.)
  @NotEmpty
  private String operation;

  // The arguments to be provided for the operation (for example, for EXECUTE this is the actual
  // command to be executed).
  private String args;

  // A description for this command.
  private String description;

  private String deviceHardwareId;
}
