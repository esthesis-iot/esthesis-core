package esthesis.platform.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class UserDTO extends BaseDTO {

  @NotNull
  private String email;

  @NotNull
  private String password;

  @NotNull
  private String salt;

  private String fn;

  private String ln;

  @NotNull
  private boolean state;
}
