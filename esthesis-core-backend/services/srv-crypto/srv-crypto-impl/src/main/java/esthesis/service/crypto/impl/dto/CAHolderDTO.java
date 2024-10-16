package esthesis.service.crypto.impl.dto;

import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bouncycastle.asn1.x509.Certificate;

/**
 * An encapsulation of a CA consisting of a public key, a private key and a certificate.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CAHolderDTO {

	// The certificate of the key.
	private Certificate certificate;

	// The public key of the key.
	private PublicKey publicKey;

	// The private key of the key.
	private PrivateKey privateKey;

}
