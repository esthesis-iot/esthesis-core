package esthesis.service.dataflow.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DataflowResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DataflowResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<DataflowEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	DataflowEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	DataflowEntity save(@Valid DataflowEntity dataflowEntity);

	@GET
	@Path("/v1/docker-tags/{dflType}")
	@RolesAllowed(AppConstants.ROLE_USER)
	DockerTagsDTO getImageTags(@PathParam("dflType") String dflType);

	@GET
	@Path("/v1/namespaces")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getNamespaces();
}
