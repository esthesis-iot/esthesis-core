package esthesis.service.crypto.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
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
@MongoEntity(collection = "Certificate")
public class CertificateEntity extends BaseEntity {

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

  // A name to identify this certificate by. This information is not part of the information
  // stored inside the certificate.
  private String name;

}
