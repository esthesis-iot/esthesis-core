package esthesis.service.crypto.resource;

import esthesis.common.crypto.dto.CreateCertificateRequestDTO;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for key services.
 */
@AccessToken
@Path("/api/key")
@RegisterRestClient(configKey = "KeyResource")
public interface KeyResource {

	/**
	 * Generate a key pair.
	 *
	 * @return The generated key pair.
	 * @throws NoSuchProviderException  If the provider is not found.
	 * @throws NoSuchAlgorithmException If the algorithm is not found.
	 */
	@GET
	@Path("/v1/keypair")
	KeyPair generateKeyPair()
	throws NoSuchAlgorithmException, NoSuchProviderException;

	/**
	 * Convert a public key to PEM format.
	 *
	 * @param publicKey The public key to convert.
	 * @return The public key in PEM format.
	 * @throws IOException If an I/O error occurs.
	 */
	@GET
	@Path("/v1/publicKeyToPEM")
	String publicKeyToPEM(PublicKey publicKey) throws IOException;

	/**
	 * Convert a private key to PEM format.
	 *
	 * @param keyPair The private key to convert.
	 * @return The private key in PEM format.
	 * @throws IOException If an I/O error occurs.
	 */
	@GET
	@Path("/v1/privateKeyToPEM")
	String privateKeyToPEM(PrivateKey keyPair) throws IOException;

	/**
	 * Generates a certificate as PEM.
	 *
	 * @param createCertificateRequestDTO The request to generate the certificate.
	 * @return The generated certificate in PEM format.
	 * @throws NoSuchAlgorithmException  If the algorithm is not found.
	 * @throws InvalidKeySpecException   If the key specification is invalid.
	 * @throws OperatorCreationException If the operator cannot be created.
	 * @throws IOException               If an I/O error occurs.
	 */
	@GET
	@Path("/v1/certificate")
	String generateCertificateAsPEM(CreateCertificateRequestDTO createCertificateRequestDTO)
	throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException;
}
