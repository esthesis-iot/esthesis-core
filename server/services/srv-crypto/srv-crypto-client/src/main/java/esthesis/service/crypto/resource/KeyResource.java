package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.request.CreateCertificateRequest;
import esthesis.service.crypto.dto.response.CreateKeyPairResponse;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "KeyResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KeyResource {

  @GET
  @Path("/v1/key/keypair")
  CreateKeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException;

  @GET
  @Path("/v1/key/publicKeyToPEM")
  String publicKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/v1/key/privateKeyToPEM")
  String privateKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/v1/key/certificate")
  String generateCertificateAsPEM(
      CreateCertificateRequest createCertificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException;
}
