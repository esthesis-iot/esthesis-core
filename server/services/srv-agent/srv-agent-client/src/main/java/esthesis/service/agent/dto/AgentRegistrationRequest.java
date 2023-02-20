package esthesis.service.agent.dto;

import esthesis.common.AppConstants.DeviceType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class AgentRegistrationRequest {

  // The hardware ID of the device.
  private String hardwareId;

  // Comma-separated list of tag names.
  private String tags;

  // The type of the device being registered.
  private DeviceType type;

}
