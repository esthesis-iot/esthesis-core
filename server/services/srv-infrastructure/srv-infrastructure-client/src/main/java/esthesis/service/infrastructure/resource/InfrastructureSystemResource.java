package esthesis.service.infrastructure.resource;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "InfrastructureSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface InfrastructureSystemResource {

  /**
   * Finds an MQTT server having at least one of the provided tags.
   *
   * @param tags The list of tag names to search by, comma separated.
   */
  @GET
  @Path("/v1/infrastructure-system/mqtt/match-by-tag")
  Optional<InfrastructureMqttEntity> matchMqttServerByTags(@QueryParam("tags") String tags);

}
