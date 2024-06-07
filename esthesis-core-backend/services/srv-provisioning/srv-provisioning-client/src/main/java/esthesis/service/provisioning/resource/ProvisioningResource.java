package esthesis.service.provisioning.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
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
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<ProvisioningPackageEntity> find(@BeanParam Pageable provisioningPackage);

	@GET
	@Path("/v1/recache")
	@RolesAllowed(AppConstants.ROLE_USER)
	void recacheAll();

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	ProvisioningPackageEntity findById(@PathParam("id") String provisioningPackageId);

	/**
	 * Recaches a previously uploaded provisioning package.
	 *
	 * @param provisioningPackageId The id of the provisioning package to recache.
	 * @return Returns the expected number of bytes to be cached, if known.
	 */
	@GET
	@Path("/v1/{id}/recache")
	@RolesAllowed(AppConstants.ROLE_USER)
	void recache(@PathParam("id") String provisioningPackageId);

	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	ProvisioningPackageEntity save(@MultipartForm ProvisioningPackageForm provisioningPackageForm);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	void delete(@PathParam("id") String provisioningPackageId);

	@GET
	@Path("/v1/{id}/download")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Uni<RestResponse<byte[]>> download(@PathParam("id") String provisioning);

	@GET
	@Path("/v1/find/by-tags")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<ProvisioningPackageEntity> findByTags(@QueryParam("tags") String tags);
}
