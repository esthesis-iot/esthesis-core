package esthesis.service.device.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceKey extends BaseDTO {

  private String publicKey;
  private String privateKey;
  private String certificate;
  private String certificateCaId;
  private Instant rolledOn;
  private boolean rolledAccepted;
}
