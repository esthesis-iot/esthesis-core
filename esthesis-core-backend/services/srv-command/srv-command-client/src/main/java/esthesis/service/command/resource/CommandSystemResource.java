package esthesis.service.command.resource;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@OidcClientFilter
@Path("/api/system-command")
@RegisterRestClient(configKey = "CommandSystemResource")
public interface CommandSystemResource {

	@GET
	@Path("/v1/reply/{correlationId}")
	List<CommandReplyEntity> getReplies(@PathParam("correlationId") String correlationId);

	/**
	 * Saves and executes a command request and directly returns the correlation ID, so the results of
	 * this command can be obtained later.
	 *
	 * @param request The command request to save and execute.
	 * @return The request scheduled info, including the correlation ID of the command request.
	 */
	@POST
	@Path("/v1")
	ExecuteRequestScheduleInfoDTO save(CommandRequestEntity request);


	/**
	 *  Find and count the number of collected replies for a given correlation ID
	 * @param correlationId The correlation ID of the command request.
	 * @return The number of collected replies found for the provided correlation ID
	 */
	@GET
	@Path("/v1/reply/count/{correlationId}")
	long countCollectedReplies(@PathParam("correlationId") String correlationId);

}
