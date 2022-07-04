package esthesis.service.crypto.resource;

import esthesis.service.crypto.dto.CertificateRequest;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/crypto")
@RegisterRestClient(configKey = "CryptoResource")
public interface CryptoResource {

  @GET
  @Path("/keypair")
  KeyPair generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException;

  @GET
  @Path("/publicKeyToPEM")
  String publicKeyToPEM(KeyPair keyPair) throws IOException;

  @GET
  @Path("/privateKeyToPEM")
  String privateKeyToPEM(KeyPair keyPair) throws IOException;

  @GET
  @Path("/certificate")
  String generateCertificateAsPEM(CertificateRequest certificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException;
}
