package esthesis.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Device extends BaseDTO {

  private String hardwareId;

  private String state;

  private List<String> tags;

  private String publicKey;

  private String privateKey;

  private String certificate;

  private Instant lastSeen;
}
