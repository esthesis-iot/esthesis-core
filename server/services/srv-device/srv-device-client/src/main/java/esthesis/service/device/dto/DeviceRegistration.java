package esthesis.service.device.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
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
public class DeviceRegistration {

  @NotNull
  private String ids;

  // The list of tag names this device supports.
  @Singular
  private List<String> tags;
}
