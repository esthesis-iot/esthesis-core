package esthesis.service.crypto.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Certificate extends BaseDTO {

  @NotNull
  @Size(max = 256)
  private String cn;

  private Instant issued;

  @NotNull
  private Instant validity;

  private String publicKey;

  private String privateKey;

  private String certificate;

  private String san;

  private String issuer;

  private int type;

}
