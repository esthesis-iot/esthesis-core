package esthesis.service.infrastructure.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/mqtt")
@RegisterRestClient(configKey = "InfrastructureResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface InfrastructureMqttResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<InfrastructureMqttEntity> find(@BeanParam Pageable pageable);

	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	InfrastructureMqttEntity save(@Valid InfrastructureMqttEntity mqttEntity);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	InfrastructureMqttEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response delete(@PathParam("id") String id);

}
