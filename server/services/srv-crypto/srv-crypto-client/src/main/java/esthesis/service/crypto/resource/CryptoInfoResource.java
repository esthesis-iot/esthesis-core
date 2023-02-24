package esthesis.service.crypto.resource;

import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/crypto-info")
@RegisterRestClient(configKey = "CryptoInfoResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
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
