package esthesis.service.crypto.resource;

import esthesis.common.crypto.dto.SignatureVerificationRequestDTO;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for signing system services.
 */
@OidcClientFilter
@Path("/api/system-signing")
@RegisterRestClient(configKey = "SigningSystemResource")
public interface SigningSystemResource {

	/**
	 * Verify a signature.
	 *
	 * @param request The request containing the signature to verify.
	 * @return True if the signature is valid, false otherwise.
	 * @throws NoSuchAlgorithmException If the algorithm is not found.
	 * @throws InvalidKeySpecException  If the key specification is invalid.
	 * @throws SignatureException       If the signature is invalid.
	 * @throws InvalidKeyException      If the key is invalid.
	 */
	@POST
	@Path("/v1/verify-signature")
	boolean verifySignature(@Valid SignatureVerificationRequestDTO request)
	throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException;

}
