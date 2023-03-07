package esthesis.service.provisioning.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import java.util.List;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api")
@RegisterRestClient(configKey = "ProvisioningResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface ProvisioningResource {

  @GET
  @Path("/v1/find")
  Page<ProvisioningPackageEntity> find(@BeanParam Pageable provisioningPackage);

  @GET
  @Path("/v1/recache")
  void recacheAll();

  @GET
  @Path("/v1/{id}")
  ProvisioningPackageEntity findById(@PathParam("id") String provisioningPackageId);

  /**
   * Recaches a previously uploaded provisioning package.
   *
   * @param provisioningPackageId The id of the provisioning package to recache.
   * @return Returns the expected number of bytes to be cached, if known.
   */
  @GET
  @Path("/v1/{id}/recache")
  void recache(@PathParam("id") String provisioningPackageId);

  @POST
  @Path("/v1")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  ProvisioningPackageEntity save(@MultipartForm ProvisioningPackageForm provisioningPackageForm);

  @DELETE
  @Path("/v1/{id}")
  void delete(@PathParam("id") String provisioningPackageId);

  @GET
  @Path("/v1/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Uni<RestResponse<byte[]>> download(@PathParam("id") String provisioning);

  @GET
  @Path("/v1/find/by-tags")
  List<ProvisioningPackageEntity> findByTags(@QueryParam("tags") String tags);
}
