package esthesis.platform.server.dto;

import esthesis.extension.device.config.AppConstants;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CommandRequestDTO extends BaseDTO {

  @NotNull
  private AppConstants.MqttCommand command;

  @NotNull
  private String description;

  private Long device;

  private String hardwareId;
}
