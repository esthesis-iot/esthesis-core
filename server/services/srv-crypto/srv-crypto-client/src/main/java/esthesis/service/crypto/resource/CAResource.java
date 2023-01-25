package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.form.ImportCaForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/ca")
@RegisterRestClient(configKey = "CAResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CAResource {

  @GET
  @Path("/v1/find")
  Page<CaEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  CaEntity findById(@PathParam("id") String id);

  @GET
  @Path("/v1/eligible-for-signing")
  List<CaEntity> getEligbleForSigning();

  @GET
  @Path("/v1/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") String caId, @QueryParam("type") AppConstants.KeyType type);

  @POST
  @Path("/v1/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  CaEntity importCa(@MultipartForm ImportCaForm input);

  @DELETE
  @Path("/v1/{id}")
  void delete(@PathParam("id") String id);

  @POST
  @Path("/v1")
  CaEntity save(@Valid CaEntity object);

  @GET
  @Path("/v1/{caId}/certificate")
  String getCACertificate(@PathParam("caId") String caId);
}
