package esthesis.platform.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ContainerPortDTO {
  private int host;
  private int container;
  private String protocol;
}
