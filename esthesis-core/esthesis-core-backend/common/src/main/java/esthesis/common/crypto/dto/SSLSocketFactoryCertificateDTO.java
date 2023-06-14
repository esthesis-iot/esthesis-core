package esthesis.common.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The representation of a certificate in PEM format to be used while creating an SSL socket
 * factory.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SSLSocketFactoryCertificateDTO {

	// The name of the certificate to be included in the keystore.
	private String name;

	// The certificate to include in the keystore in PEM format.
	private String pemCertificate;
}
