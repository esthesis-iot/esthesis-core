package esthesis.service.crypto.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@RegisterForReflection
@Accessors(chain = true)
public class CreateKeyPairResponse {

  public byte[] publicKey;
  public byte[] privateKey;
}
