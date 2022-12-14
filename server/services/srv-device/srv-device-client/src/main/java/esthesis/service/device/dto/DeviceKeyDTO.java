package esthesis.service.device.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DeviceKeyDTO {

  private String publicKey;
  private String privateKey;
  private String certificate;
  private String certificateCaId;
  private Instant rolledOn;
  private boolean rolledAccepted;
}
