package esthesis.service.device.dto;

import esthesis.common.AppConstants.DeviceType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode
public class DeviceRegistrationDTO {

  @NotBlank
  private String hardwareId;

  // The list of tag names this device supports.
  @Singular
  private List<String> tags;

  // The type of the device being registered.
  @NotNull
  private DeviceType type;

  // The optional registration secret, when the platform operates in that mode.
  private String registrationSecret;
}
