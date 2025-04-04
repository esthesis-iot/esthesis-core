package esthesis.service.crypto.resource;

import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for Crypto services.
 */
@AccessToken
@Path("/api/crypto-info")
@RegisterRestClient(configKey = "CryptoInfoResource")
public interface CryptoInfoResource {

	/**
	 * Get the supported keystore types.
	 *
	 * @return the supported keystore types.
	 */
	@GET
	@Path("/v1/keystore-types")
	List<String> getSupportedKeystoreTypes();

	/**
	 * Get the supported key types.
	 *
	 * @return the supported key types.
	 */
	@GET
	@Path("/v1/key-algorithms")
	List<String> getSupportedKeyAlgorithms();

	/**
	 * Get the supported signature algorithms.
	 *
	 * @return the supported signature algorithms.
	 */
	@GET
	@Path("/v1/signature-algorithms")
	List<String> getSupportedSignatureAlgorithms();

	/**
	 * Get the supported message digest algorithms.
	 *
	 * @return the supported message digest algorithms.
	 */
	@GET
	@Path("/v1/message-digest-algorithms")
	List<String> getSupportedMessageDigestAlgorithms();

	/**
	 * Get the supported key agreement algorithms.
	 *
	 * @return the supported key agreement algorithms.
	 */
	@GET
	@Path("/v1/key-agreement-algorithms")
	List<String> getSupportedKeyAgreementAlgorithms();
}
