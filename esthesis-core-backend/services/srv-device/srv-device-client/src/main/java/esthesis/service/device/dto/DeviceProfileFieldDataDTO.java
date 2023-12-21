package esthesis.service.device.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DeviceProfileFieldDataDTO implements Serializable {

  private String label;
  private String value;
  private String valueType;
  private Instant lastUpdate;
  private String icon;
}
