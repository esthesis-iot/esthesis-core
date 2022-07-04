package esthesis.service.device.dto;

import esthesis.common.dto.GenericRequest;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class RegistrationRequest implements GenericRequest {

  // Comma-separated list of tags.
  private String tags;

}
