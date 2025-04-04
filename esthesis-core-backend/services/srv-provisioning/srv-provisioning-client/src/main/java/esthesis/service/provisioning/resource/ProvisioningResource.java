package esthesis.service.provisioning.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
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
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * REST client for the provisioning service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "ProvisioningResource")
public interface ProvisioningResource {

	/**
	 * Find provisioning packages.
	 *
	 * @param pageable poging parameters.
	 * @return a page of provisioning packages.
	 */
	@GET
	@Path("/v1/find")
	Page<ProvisioningPackageEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds multiple provisioning packages by their ids.
	 *
	 * @param ids A comma-separated list of ids.
	 * @return A list of provisioning packages with the given ids.
	 */
	@GET
	@Path("/v1/find/by-ids")
	List<ProvisioningPackageEntity> findByIds(@QueryParam("ids") String ids);

	/**
	 * Find a provisioning package by id.
	 *
	 * @param provisioningPackageId the id of the provisioning package.
	 * @return the provisioning package.
	 */
	@GET
	@Path("/v1/{id}")
	ProvisioningPackageEntity findById(@PathParam("id") String provisioningPackageId);

	/**
	 * Save a provisioning package.
	 *
	 * @param provisioningPackageEntity the provisioning package.
	 * @param file                      the file representing the binary content of the provisioning
	 *                                  package.
	 * @return the saved provisioning package.
	 */
	@POST
	@Path("/v1")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	ProvisioningPackageEntity save(
		@Valid @RestForm("dto") @PartType(MediaType.APPLICATION_JSON) ProvisioningPackageEntity provisioningPackageEntity,
		@RestForm("file") FileUpload file);

	/**
	 * Deletes a provisioning package.
	 *
	 * @param provisioningPackageId the id of the provisioning package.
	 */
	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String provisioningPackageId);

	/**
	 * Download a provisioning package.
	 *
	 * @param provisioning the id of the provisioning package.
	 * @return the binary content of the provisioning package.
	 */
	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Uni<RestResponse<byte[]>> download(@PathParam("id") String provisioning);

	/**
	 * Find provisioning packages by tags.
	 *
	 * @param tags the tag IDs to search by.
	 * @return a list of provisioning packages.
	 */
	@GET
	@Path("/v1/find/by-tags")
	List<ProvisioningPackageEntity> findByTags(@QueryParam("tags") String tags);
}
