package esthesis.backend.dto;

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
  private String tags;
  private String hardwareIds;
}
