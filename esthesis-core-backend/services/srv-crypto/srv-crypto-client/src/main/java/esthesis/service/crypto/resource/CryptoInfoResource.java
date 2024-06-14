package esthesis.service.crypto.resource;

import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api/crypto-info")
@RegisterRestClient(configKey = "CryptoInfoResource")
public interface CryptoInfoResource {

	@GET
	@Path("/v1/keystore-types")
	List<String> getSupportedKeystoreTypes();

	@GET
	@Path("/v1/key-algorithms")
	List<String> getSupportedKeyAlgorithms();

	@GET
	@Path("/v1/signature-algorithms")
	List<String> getSupportedSignatureAlgorithms();

	@GET
	@Path("/v1/message-digest-algorithms")
	List<String> getSupportedMessageDigestAlgorithms();

	@GET
	@Path("/v1/key-agreement-algorithms")
	List<String> getSupportedKeyAgreementAlgorithms();
}
