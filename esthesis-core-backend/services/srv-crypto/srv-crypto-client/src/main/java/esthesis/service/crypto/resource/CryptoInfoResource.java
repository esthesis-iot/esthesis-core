package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/crypto-info")
@RegisterRestClient(configKey = "CryptoInfoResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CryptoInfoResource {

	@GET
	@Path("/v1/keystore-types")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getSupportedKeystoreTypes();

	@GET
	@Path("/v1/key-algorithms")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getSupportedKeyAlgorithms();

	@GET
	@Path("/v1/signature-algorithms")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getSupportedSignatureAlgorithms();

	@GET
	@Path("/v1/message-digest-algorithms")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getSupportedMessageDigestAlgorithms();

	@GET
	@Path("/v1/key-agreement-algorithms")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getSupportedKeyAgreementAlgorithms();

}
