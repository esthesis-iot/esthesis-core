package esthesis.platform.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ContainerEnvDTO {
  private String envName;
  private String envValue;
}
