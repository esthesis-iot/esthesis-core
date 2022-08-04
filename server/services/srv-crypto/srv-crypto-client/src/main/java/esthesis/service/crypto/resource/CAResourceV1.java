package esthesis.service.crypto.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.form.ImportCaForm;
import esthesis.service.crypto.dto.request.CreateCertificateRequest;
import esthesis.service.crypto.dto.response.CreateKeyPairResponse;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/v1/ca")
@RegisterRestClient(configKey = "CAResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CAResourceV1 {

  @GET
  @Path("/keypair")
  CreateKeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException;

  @GET
  @Path("/publicKeyToPEM")
  String publicKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/privateKeyToPEM")
  String privateKeyToPEM(byte[] keyPair) throws IOException;

  @GET
  @Path("/certificate")
  String generateCertificateAsPEM(
      CreateCertificateRequest createCertificateRequest)
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
  @Path("/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") ObjectId id);

  @POST
  @Path("/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Ca importCa(@MultipartForm ImportCaForm input);

  @DELETE
  @Path("/{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST()
  Ca save(@Valid Ca object);
}
