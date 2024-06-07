package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.KeystoreEntity;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/keystore")
@RegisterRestClient(configKey = "KeystoreResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KeystoreResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<KeystoreEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	KeystoreEntity findById(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	KeystoreEntity save(@Valid KeystoreEntity keystoreEntity);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	void delete(@PathParam("id") String id);

	@GET
	@Path("/v1/{id}/download")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String id);

}
