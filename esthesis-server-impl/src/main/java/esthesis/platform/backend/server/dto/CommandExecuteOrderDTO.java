package esthesis.platform.backend.server.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CommandExecuteOrderDTO {
  @NotEmpty
  private String command;
  private String arguments;
  private String description;
  // Comma-separated list of tags to search devices to execute the command on.
  private String tags;
  // Comma-separated list of hardware Ids to execute the command on.
  private String hardwareIds;
}
