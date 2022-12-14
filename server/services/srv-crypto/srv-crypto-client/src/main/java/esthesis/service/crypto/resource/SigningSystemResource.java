package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.SignatureVerificationRequestDTO;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "SigningSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface SigningSystemResource {

  @POST
  @Path("/v1/signing-system/verify-signature")
  boolean verifySignature(@Valid SignatureVerificationRequestDTO request)
  throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException;

}
