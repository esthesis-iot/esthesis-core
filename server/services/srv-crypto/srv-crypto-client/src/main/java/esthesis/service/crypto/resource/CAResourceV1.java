package esthesis.service.crypto.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.dto.KeyPairResponse;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/ca")
@RegisterRestClient(configKey = "CAResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CAResourceV1 {

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

  @GET
  @Path("/find")
  Page<Ca> find(@BeanParam Pageable pageable);

  @GET
  @Path("/{id}")
  Ca findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/eligible-for-signing")
  List<Ca> getEligbleForSigning();

  @GET
  @Path("/{id}/download/{keyType}")
  Response download(@PathParam("id") ObjectId id,
      @PathParam("keyType") String keyType);

  @GET
  @Path(value = "/{id}/backup")
  Response backup(@PathParam("id") ObjectId id);

//  @POST
//  @Path("/restore")
//  Response restore(@BeanParam MultipartFile backup);

  @DELETE
  @Path("/{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST()
  Ca save(@Valid Ca object);
}
