package esthesis.service.tag.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "TagResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface TagResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<TagEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/get-all")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<TagEntity> getAll();

	/**
	 * Finds a tag by its name.
	 *
	 * @param name         The name of the tag to search.
	 * @param partialMatch If true, the search will be performed using a partial match.
	 */
	@GET
	@Path("/v1/find/by-name/{name}")
	@RolesAllowed(AppConstants.ROLE_USER)
	TagEntity findByName(@PathParam("name") String name,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	/**
	 * Finds multiple tags by their names.
	 *
	 * @param name         A comma-separated list of names.
	 * @param partialMatch If true, the search will be performed using a partial match.
	 */
	@GET
	@Path("/v1/find/by-names")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<TagEntity> findByNames(@QueryParam("names") String name,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	TagEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	TagEntity save(@Valid TagEntity tagEntity);
}
