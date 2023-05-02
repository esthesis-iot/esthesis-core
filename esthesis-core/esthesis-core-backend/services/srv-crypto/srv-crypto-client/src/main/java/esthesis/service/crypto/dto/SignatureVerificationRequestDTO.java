package esthesis.service.crypto.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignatureVerificationRequestDTO {

	// The public key to verify the signature with, in PEM format.
	@NotEmpty
	private String publicKey;

	// The content for which this signature was created.
	@NotEmpty
	private byte[] payload;

	// The signature to verify, Base64 encoded.
	@NotEmpty
	private String signature;

	// The algorithm used to create the signature, e.g. SHA256withRSA.
	@NotEmpty
	private String signatureAlgorithm;

	// The algorithm used to create the public key, e.g. RSA.
	@NotEmpty
	private String keyAlgorithm;
}
