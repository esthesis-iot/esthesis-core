package esthesis.platform.server.dto;

import esthesis.platform.common.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DeviceKeyDTO extends BaseDTO  {
  private String publicKey;
  private String privateKey;
  private String sessionKey;
  private String psPublicKey;
}
