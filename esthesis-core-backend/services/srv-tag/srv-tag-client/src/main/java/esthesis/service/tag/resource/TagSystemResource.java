package esthesis.service.tag.resource;

import esthesis.common.AppConstants;
import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "TagSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface TagSystemResource {

	@GET
	@Path("/v1/system/get-all")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	List<TagEntity> getAll();

	/**
	 * Finds a tag by its name.
	 *
	 * @param name The name of the tag to search.
	 */
	@GET
	@Path("/v1/system/find/by-name/{name}")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	TagEntity findByName(@PathParam("name") String name);
}
