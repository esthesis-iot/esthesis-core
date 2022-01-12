package esthesis.platform.backend.server.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class WfVersionDTO {
  private String version;
}
