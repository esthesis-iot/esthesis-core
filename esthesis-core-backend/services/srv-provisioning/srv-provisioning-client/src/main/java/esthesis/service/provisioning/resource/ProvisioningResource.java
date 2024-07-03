package esthesis.service.provisioning.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
import io.smallrye.common.annotation.Blocking;
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

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "ProvisioningResource")
public interface ProvisioningResource {

	@GET
	@Path("/v1/find")
	Page<ProvisioningPackageEntity> find(@BeanParam Pageable provisioningPackage);

	@GET
	@Path("/v1/{id}")
	ProvisioningPackageEntity findById(@PathParam("id") String provisioningPackageId);

	@POST
	@Path("/v1")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	ProvisioningPackageEntity save(
		@Valid @RestForm("dto") @PartType(MediaType.APPLICATION_JSON) ProvisioningPackageEntity provisioningPackageEntity,
		@RestForm("file") FileUpload file);

	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String provisioningPackageId);

	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Blocking
	Uni<RestResponse<byte[]>> download(@PathParam("id") String provisioning);

	@GET
	@Path("/v1/find/by-tags")
	List<ProvisioningPackageEntity> findByTags(@QueryParam("tags") String tags);
}
