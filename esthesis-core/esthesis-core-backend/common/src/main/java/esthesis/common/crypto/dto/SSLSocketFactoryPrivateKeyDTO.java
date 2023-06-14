package esthesis.common.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The representation of a private key in PEM format to be used while creating an SSL socket
 * factory.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SSLSocketFactoryPrivateKeyDTO {

	// The name of the private key to be included in the keystore.
	private String name;

	// The private key in PEM format.
	private String pemPrivateKey;

	// The algorithm with which the private key was initially generated.
	private String algorithm;
}
