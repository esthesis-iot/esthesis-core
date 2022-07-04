package esthesis.service.crypto.impl.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCA {

  @NotNull
  private CreateKeyPair createKeyPairRequest;
  @NotNull
  private String subjectCN;
  @NotNull
  private String signatureAlgorithm;
  @NotNull
  private BigInteger serial;
  @NotNull
  private Instant validFrom;
  @NotNull
  private Instant validTo;
  @NotNull
  private Locale locale;

  private String issuerCN;
  private String issuerPrivateKey;
  private String issuerPrivateKeyAlgorithm;
}
