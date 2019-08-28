package esthesis.platform.server.dto;

import esthesis.common.config.AppConstants.MqttCommand;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class CommandSpecificationDTO {
  private String hardwareIds;
  private String tags;
  private MqttCommand command;
  private String arguments;
  private String description;
}
