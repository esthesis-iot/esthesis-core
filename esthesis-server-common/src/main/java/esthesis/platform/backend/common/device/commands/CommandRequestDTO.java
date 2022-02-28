package esthesis.platform.backend.common.device.commands;

import esthesis.platform.backend.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommandRequestDTO extends BaseDTO {

  // The operation this command is about (for example, PING, EXECUTE, etc.)
  @NotEmpty
  @Schema(description = "The operation this command is about (for example, PING, EXECUTE, etc.)")
  private String operation;

  // The arguments to be provided for the operation (for example, for EXECUTE this is the actual
  // command to be executed).

  @Schema(description = "The arguments to be provided for the operation.")
  private String args;

  // A description for this command.
  @Schema(description = "A description for this command.")
  private String description;


  @Schema(description = "The hardwareId of the device on which the command will be executed.")
  private String deviceHardwareId;
}
