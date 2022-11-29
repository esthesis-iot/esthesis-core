package esthesis.service.provisioning.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api")
@RegisterRestClient(configKey = "ProvisioningResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface ProvisioningResource {

  @GET
  @Path("/v1/provisioning/find")
  Page<ProvisioningPackage> find(@BeanParam Pageable provisioningPackage);

  @GET
  @Path("/v1/provisioning/{id}")
  ProvisioningPackage findById(@PathParam("id") ObjectId provisioningPackageId);

  /**
   * Recaches a previously uploaded provisioning package.
   *
   * @param provisioningPackageId The id of the provisioning package to recache.
   * @return Returns the expected number of bytes to be cached, if known.
   */
  @GET
  @Path("/v1/provisioning/{id}/recache")
  long recache(@PathParam("id") ObjectId provisioningPackageId);

  @POST
  @Path("/v1/provisioning")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  ProvisioningPackage save(@MultipartForm ProvisioningPackageForm provisioningPackageForm);

}
