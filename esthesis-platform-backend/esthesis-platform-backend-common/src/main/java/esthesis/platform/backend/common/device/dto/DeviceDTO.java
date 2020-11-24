package esthesis.platform.backend.common.device.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceDTO extends BaseDTO {

  @NotNull
  private String hardwareId;

  @NotNull
  private String state;

  @Singular
  private List<Long> tags;

  private String publicKey;
  private String privateKey;
  private String sessionKey;
  private String psPublicKey;
  private String provisioningKey;
  private String certificate;
  private Instant lastSeen;
}
