package esthesis.service.command.resource;

import esthesis.common.AppConstants;
import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/system-command")
@RegisterRestClient(configKey = "CommandSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface CommandSystemResource {

	@GET
	@Path("/v1/reply/{correlationId}")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
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
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	String save(CommandRequestEntity request);
}
