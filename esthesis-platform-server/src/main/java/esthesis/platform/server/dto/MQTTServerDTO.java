package esthesis.platform.server.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MQTTServerDTO extends BaseDTO {

  @NotNull
  public String name;
  @NotNull
  public String ipAddress;
  public String aIpAddress;
  @NotNull
  public boolean state;
  @Singular
  private List<Long> tags;
  private String caCert;
  private String clientCert;
  private String clientKey;

  public boolean getState() {
    return state;
  }
}


