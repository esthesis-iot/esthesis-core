package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.dto.KeyPairResponse;
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

@Path("/api/v1/crypto")
@RegisterRestClient(configKey = "CryptoResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CryptoResourceV1 {

  @GET
  @Path("/keypair")
  KeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException;

  @GET
  @Path("/publicKeyToPEM")
  String publicKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/privateKeyToPEM")
  String privateKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/certificate")
  String generateCertificateAsPEM(CertificateRequest certificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException;
}
