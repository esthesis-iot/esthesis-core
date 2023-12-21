package esthesis.service.crypto.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of a certificate.
 */
@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Certificate")
public class CertificateEntity extends BaseEntity {

	// The common name (CN) of the subject of the certificate.
	@NotNull
	@Size(max = 256)
	private String cn;

	// The date and time when the certificate was issued.
	private Instant issued;

	// The date and time when the certificate expires.
	@NotNull
	private Instant validity;

	// The public key in PEM format.
	private String publicKey;

	// The private key in PEM format.
	private String privateKey;

	// The certificate in PEM format.
	private String certificate;

	// A comma-separated list of SANs that will be added to the certificate as a Subject
	// Alternative Names of type DNS.
	private String san;

	// The issuer of this certificate.
	private String issuer;

	// The algorithm used to generate the key pair.
	private String keyAlgorithm;

	// The algorithm used to create the signature, e.g. SHA256withRSA.
	private String signatureAlgorithm;

	// A name to identify this certificate by. This information is not part of the information
	// stored inside the certificate.
	@NotNull
	@Size(max = 256)
	private String name;

}
