package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ApplicationDTO extends BaseDTO {

  @NotNull
  private String name;

  @NotNull
  private String token;

  @NotNull
  private boolean state;

  private String userId;
}
