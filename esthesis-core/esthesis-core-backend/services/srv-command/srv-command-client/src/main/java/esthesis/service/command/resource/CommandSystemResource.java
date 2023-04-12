package esthesis.service.command.resource;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/system-command")
@RegisterRestClient(configKey = "CommandSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface CommandSystemResource {

  @GET
  @Path("/v1/reply/{correlationId}")
  List<CommandReplyEntity> getReplies(@PathParam("correlationId") String correlationId);

  /**
   * Saves and executes a command request and directly returns the correlation ID, so the results of
   * this command can be obtained later.
   *
   * @param request The command request to save and execute.
   * @return The correlation ID of the command request.
   */
  @POST
  @Path("/v1")
  String save(CommandRequestEntity request);

}
