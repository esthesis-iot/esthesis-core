package esthesis.service.device.dto.agent;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class RegistrationRequest {

  private String hardwareId;

  // Comma-separated list of tags.
  private String tags;

}
