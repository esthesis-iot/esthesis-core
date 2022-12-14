package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
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
  KeyPair generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException;

  @GET
  @Path("/v1/key/publicKeyToPEM")
  String publicKeyToPEM(PublicKey publicKey) throws IOException;

  @GET
  @Path("/v1/key/privateKeyToPEM")
  String privateKeyToPEM(PrivateKey keyPair) throws IOException;

  @GET
  @Path("/v1/key/certificate")
  String generateCertificateAsPEM(
      CreateCertificateRequestDTO createCertificateRequestDTO)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException;
}
