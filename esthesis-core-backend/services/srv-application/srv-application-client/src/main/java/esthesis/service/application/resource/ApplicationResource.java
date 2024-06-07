package esthesis.service.application.resource;

import esthesis.common.AppConstants;
import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ApplicationResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface ApplicationResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<ApplicationEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	ApplicationEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	ApplicationEntity save(@Valid ApplicationEntity applicationEntity);
}
