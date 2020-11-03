package esthesis.platform.server.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class XXXCommandRequestDTO extends BaseDTO {

  @NotNull
  private String command;

  @NotNull
  private String description;

  private Long device;

  private String hardwareId;
}
