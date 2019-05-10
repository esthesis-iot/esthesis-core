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
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AuditDTO extends BaseDTO {
  @NotNull
  private String event;

  @NotNull
  private String level;

  @NotNull
  private String description;

  @NotNull
  private UserDTO user;
}
