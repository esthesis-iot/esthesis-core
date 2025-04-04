package esthesis.service.infrastructure.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for managing MQTT infrastructure resources.
 */
@AccessToken
@Path("/api/mqtt")
@RegisterRestClient(configKey = "InfrastructureResource")
public interface InfrastructureMqttResource {

	/**
	 * Find all MQTT infrastructure resources.
	 *
	 * @param pageable paging parameters.
	 * @return a page of infrastructure resources.
	 */
	@GET
	@Path("/v1/find")
	Page<InfrastructureMqttEntity> find(@BeanParam Pageable pageable);

	/**
	 * Save a new MQTT infrastructure resource.
	 *
	 * @param mqttEntity the MQTT infrastructure resource to save.
	 * @return the saved MQTT infrastructure resource.
	 */
	@POST
	@Path("/v1")
	InfrastructureMqttEntity save(@Valid InfrastructureMqttEntity mqttEntity);

	/**
	 * Find a MQTT infrastructure resource by its id.
	 *
	 * @param id the id of the MQTT infrastructure resource to find.
	 * @return the MQTT infrastructure resource.
	 */
	@GET
	@Path("/v1/{id}")
	InfrastructureMqttEntity findById(@PathParam("id") String id);

	/**
	 * Delete a MQTT infrastructure resource by its id.
	 *
	 * @param id the id of the MQTT infrastructure resource to delete.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);
}
