package esthesis.service.crypto.dto;

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
public class KeyPairResponse {

  public byte[] publicKey;
  public byte[] privateKey;
}
