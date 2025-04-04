package esthesis.service.tag.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for managing Tag resources.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "TagResource")
public interface TagResource {

	/**
	 * Finds all tags.
	 *
	 * @param pageable pagination parameters.
	 */
	@GET
	@Path("/v1/find")
	Page<TagEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds all tags.
	 *
	 * @return a list of all tags.
	 */
	@GET
	@Path("/v1/get-all")
	List<TagEntity> getAll();

	/**
	 * Finds a tag by its name.
	 *
	 * @param name The name of the tag to search.
	 * @return The tag with the given name.
	 */
	@GET
	@Path("/v1/find/by-name/{name}")
	TagEntity findByName(@PathParam("name") String name);

	/**
	 * Finds multiple tags by their names.
	 *
	 * @param name A comma-separated list of names.
	 * @return A list of tags with the given names.
	 */
	@GET
	@Path("/v1/find/by-names")
	List<TagEntity> findByNames(@QueryParam("names") String name);

	/**
	 * Finds a tag by its id.
	 *
	 * @param id The id of the tag to search.
	 * @return The tag with the given id.
	 */
	@GET
	@Path("/v1/{id}")
	TagEntity findById(@PathParam("id") String id);

	/**
	 * Finds multiple tags by their ids.
	 *
	 * @param id A comma-separated list of ids.
	 * @return A list of tags with the given ids.
	 */
	@GET
	@Path("/v1/find/by-ids")
	List<TagEntity> findByIds(@QueryParam("ids") String id);

	/**
	 * Deletes a tag by its id.
	 *
	 * @param id The id of the tag to delete.
	 * @return The response.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	/**
	 * Saves a tag.
	 *
	 * @param tagEntity The tag to save.
	 * @return The saved tag.
	 */
	@POST
	@Path("/v1")
	@Produces("application/json")
	TagEntity save(@Valid TagEntity tagEntity);
}
