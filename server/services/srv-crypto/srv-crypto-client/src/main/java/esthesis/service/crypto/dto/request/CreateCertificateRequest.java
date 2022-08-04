package esthesis.service.crypto.dto.request;

import esthesis.service.crypto.dto.response.CreateKeyPairResponse;
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
public class CreateCertificateRequest {

  private CreateKeyPairResponse createKeyPairResponse;
  private String cn;
}
