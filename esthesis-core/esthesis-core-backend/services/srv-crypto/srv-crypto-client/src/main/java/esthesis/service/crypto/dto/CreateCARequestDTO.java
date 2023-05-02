package esthesis.service.crypto.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCARequestDTO {

	@NotNull
	private CreateKeyPairRequestDTO createKeyPairRequestDTO;
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
	private PrivateKey issuerPrivateKey;
	private String issuerPrivateKeyAlgorithm;
}
