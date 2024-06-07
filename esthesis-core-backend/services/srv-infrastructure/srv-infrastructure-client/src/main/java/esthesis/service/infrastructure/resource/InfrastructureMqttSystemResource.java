package esthesis.service.infrastructure.resource;

import esthesis.common.AppConstants;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/system-mqtt")
@RegisterRestClient(configKey = "InfrastructureSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface InfrastructureMqttSystemResource {

	/**
	 * Finds an MQTT server having at least one of the provided tags.
	 *
	 * @param tags The list of tag names to search by, comma separated.
	 */
	@GET
	@Path("/v1/match-by-tag")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	Optional<InfrastructureMqttEntity> matchMqttServerByTags(@QueryParam("tags") String tags);

	/**
	 * Finds a random MQTT server, this is useful when devices try to register but provide no specific
	 * tags.
	 */
	@GET
	@Path("/v1/match-random")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	Optional<InfrastructureMqttEntity> matchRandomMqttServer();
}
