package esthesis.service.tag.resource;

import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "TagSystemResource")
public interface TagSystemResource {

	@GET
	@Path("/v1/system/get-all")
	List<TagEntity> getAll();

	/**
	 * Finds a tag by its name.
	 *
	 * @param name The name of the tag to search.
	 */
	@GET
	@Path("/v1/system/find/by-name/{name}")
	TagEntity findByName(@PathParam("name") String name);
}
