package esthesis.service.tag.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "TagResource")
public interface TagResource {

	@GET
	@Path("/v1/find")
	Page<TagEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/get-all")
	List<TagEntity> getAll();

	/**
	 * Finds a tag by its name.
	 *
	 * @param name         The name of the tag to search.
	 * @param partialMatch If true, the search will be performed using a partial match.
	 */
	@GET
	@Path("/v1/find/by-name/{name}")
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
	List<TagEntity> findByNames(@QueryParam("names") String name,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	@GET
	@Path("/v1/{id}")
	TagEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	TagEntity save(@Valid TagEntity tagEntity);
}
