package esthesis.device.runtime.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DevicePingMessageDTO {
  @JsonProperty("m")
  private static final String MEASUREMENT = "ping";

  @Getter
  @Setter
  @JsonProperty("v")
  private long deviceTime;
}
