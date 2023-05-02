package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.SignatureVerificationRequestDTO;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/system-signing")
@RegisterRestClient(configKey = "SigningSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface SigningSystemResource {

	@POST
	@Path("/v1/verify-signature")
	boolean verifySignature(@Valid SignatureVerificationRequestDTO request)
	throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException;

}
