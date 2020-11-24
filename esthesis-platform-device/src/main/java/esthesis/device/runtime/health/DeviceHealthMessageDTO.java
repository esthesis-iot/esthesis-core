package esthesis.device.runtime.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@Accessors(chain = true)
public class DeviceHealthMessageDTO {

  @JsonProperty("m")
  private final String MEASUREMENT = "health";

  @JsonProperty("v")
  private DeviceHealthDataDTO deviceHealthDataDTO;
}
