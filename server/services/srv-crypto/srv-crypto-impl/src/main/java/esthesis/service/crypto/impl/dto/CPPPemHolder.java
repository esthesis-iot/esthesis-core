package esthesis.service.crypto.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * An encapsulation of a key comprising of a public key, a private key and a
 * certificate in PEM format.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CPPPemHolder {

  // The certificate of the key.
  private String certificate;

  // The public key of the key.
  private String publicKey;

  // The private key of the key.
  private String privateKey;

}
