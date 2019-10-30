package esthesis.platform.server.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NiFiDTO extends BaseDTO {
  @NotNull
  private boolean state;

  @NotNull
  private String name;
  @NotNull
  private String url;
  private String description;
  private String wfVersion;
}
