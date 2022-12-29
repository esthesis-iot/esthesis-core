package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.form.ImportCertificateForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
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
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/certificate")
@RegisterRestClient(configKey = "CertificateResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CertificateResource {

  @GET
  @Path("/v1/find")
  Page<CertificateEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  CertificateEntity findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/v1/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") ObjectId certId,
      @QueryParam("type") AppConstants.KeyType type);

  @POST
  @Path("/v1/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  CertificateEntity importCertificate(@MultipartForm ImportCertificateForm input);

  @DELETE
  @Path("/v1/{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1")
  CertificateEntity save(@Valid CertificateEntity object);

}
