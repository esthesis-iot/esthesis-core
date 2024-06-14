package esthesis.service.provisioning.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import io.quarkus.oidc.token.propagation.AccessToken;
import io.smallrye.mutiny.Uni;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "ProvisioningResource")
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
