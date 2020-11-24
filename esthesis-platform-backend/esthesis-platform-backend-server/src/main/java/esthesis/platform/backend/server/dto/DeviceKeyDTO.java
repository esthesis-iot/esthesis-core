package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
public class DeviceKeyDTO extends BaseDTO {
  private String publicKey;
  private String privateKey;
  private String sessionKey;
  private String psPublicKey;
  private String certificate;
  private String certificateCaId;
}
