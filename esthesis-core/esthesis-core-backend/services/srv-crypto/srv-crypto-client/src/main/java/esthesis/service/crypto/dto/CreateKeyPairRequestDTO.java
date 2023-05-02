package esthesis.service.crypto.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A request to create a key pair.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKeyPairRequestDTO {

	// The algorithm to use while generating the key-pair, e.g. RSA.
	@NotNull
	private String keyPairGeneratorAlgorithm;

	// The provider for the requested keypair generator algorithm, e.g. RSA.
	private String keyPairGeneratorProvider;

	// The name of algorithm to use while initialising the key-pair generator, e.g. NativePRNG.
	// If you leave this field empty, a system default strong random algorithm will be chosen via
	// SecureRandom.getInstanceStrong().
	private String secureRandomAlgorithm;

	// The bits of the key, e.g. 2048.
	@NotNull
	private int keySize;
}
