package esthesis.service.crypto.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.KeystoreEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for keystore services.
 */
@AccessToken
@Path("/api/keystore")
@RegisterRestClient(configKey = "KeystoreResource")
public interface KeystoreResource {

	/**
	 * Find keystore entities.
	 *
	 * @param pageable The pageable.
	 * @return The keystore entities.
	 */
	@GET
	@Path("/v1/find")
	Page<KeystoreEntity> find(@BeanParam Pageable pageable);

	/**
	 * Find a keystore entity by ID.
	 *
	 * @param id The ID.
	 * @return The keystore entity.
	 */
	@GET
	@Path("/v1/{id}")
	KeystoreEntity findById(@PathParam("id") String id);

	/**
	 * Save a keystore entity.
	 *
	 * @param keystoreEntity The keystore entity.
	 * @return The saved keystore entity.
	 */
	@POST
	@Path("/v1")
	KeystoreEntity save(@Valid KeystoreEntity keystoreEntity);

	/**
	 * Delete a keystore entity by ID.
	 *
	 * @param id The ID.
	 */
	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String id);

	/**
	 * Download a keystore entity by ID.
	 *
	 * @param id The ID.
	 * @return The keystore entity.
	 */
	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String id);

}
