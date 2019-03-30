package esthesis.platform.server.dto;

import esthesis.platform.common.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class RedisServerDTO extends BaseDTO {

  @NotNull
  public String name;

  @NotNull
  public String ipAddress;

  @NotNull
  public int state;

  private String caCert;
  private String clientCert;
  private String clientKey;
  private String username;
  private String password;
}
