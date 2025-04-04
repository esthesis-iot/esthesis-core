package esthesis.service.application.resource;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the application service.
 */
@Path("/api")
@AccessToken
@RegisterRestClient(configKey = "ApplicationResource")
public interface ApplicationResource {

	/**
	 * Find applications.
	 *
	 * @param pageable Paging parameters for the query.
	 * @return A page of results.
	 */
	@GET
	@Path("/v1/find")
	Page<ApplicationEntity> find(@BeanParam Pageable pageable);

	/**
	 * Find an application by ID.
	 *
	 * @param id The ID of the application to find.
	 * @return The application.
	 */
	@GET
	@Path("/v1/{id}")
	ApplicationEntity findById(@PathParam("id") String id);

	/**
	 * Delete an application by ID.
	 *
	 * @param id The ID of the application to delete.
	 * @return The response.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	/**
	 * Save an application.
	 *
	 * @param applicationEntity The application to save.
	 * @return The saved application.
	 */
	@POST
	@Path("/v1")
	@Produces("application/json")
	ApplicationEntity save(@Valid ApplicationEntity applicationEntity);
}
