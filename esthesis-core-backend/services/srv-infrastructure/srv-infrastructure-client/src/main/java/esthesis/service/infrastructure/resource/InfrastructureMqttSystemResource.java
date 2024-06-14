package esthesis.service.infrastructure.resource;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@OidcClientFilter
@Path("/api/system-mqtt")
@RegisterRestClient(configKey = "InfrastructureSystemResource")
public interface InfrastructureMqttSystemResource {

	/**
	 * Finds an MQTT server having at least one of the provided tags.
	 *
	 * @param tags The list of tag names to search by, comma separated.
	 */
	@GET
	@Path("/v1/match-by-tag")
	Optional<InfrastructureMqttEntity> matchMqttServerByTags(@QueryParam("tags") String tags);

	/**
	 * Finds a random MQTT server, this is useful when devices try to register but provide no specific
	 * tags.
	 */
	@GET
	@Path("/v1/match-random")
	Optional<InfrastructureMqttEntity> matchRandomMqttServer();
}
