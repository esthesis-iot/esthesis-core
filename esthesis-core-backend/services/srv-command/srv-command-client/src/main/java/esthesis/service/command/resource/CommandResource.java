package esthesis.service.command.resource;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "CommandResource")
public interface CommandResource {

	@GET
	@Path("/v1/find")
	Page<CommandRequestEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{commandId}")
	CommandRequestEntity getCommand(@PathParam("commandId") String commandId);

	@GET
	@Path("/v1/reply/{correlationId}")
	List<CommandReplyEntity> getReply(@PathParam("correlationId") String correlationId);

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

	/**
	 * Saves and executes a command request and waits until the reply for that command is available to
	 * return it. Since a command may target multiple devices, the result is a list of replies. The
	 * amount of time to wait before declaring a timeout is configurable.
	 *
	 * @param request      The command request to save and execute.
	 * @param timeout      The amount of time (in milliseconds) to wait for obtaining the results
	 *                     before declaring a timeout. By default, the timeout is set at 3 seconds.
	 * @param pollInterval The amount of time (in milliseconds) to wait before checking if the results
	 *                     are available. By default, the poll interval is set at 500 milliseconds. A
	 *                     smaller pollInterval makes the device seem more responsive, however it may
	 *                     introduce extra load to the database.
	 * @return Returns the reply to the command. If the command timed out, an empty reply will be
	 * returned
	 */

	@POST
	@Path("/v1/wait-for-reply")
	List<CommandReplyEntity> saveAndWait(CommandRequestEntity request,
		@QueryParam("timeout") @DefaultValue("3000") long timeout,
		@QueryParam("pollInterval") @DefaultValue("500") long pollInterval);

	/**
	 * Finds devices with the given hardware IDs. The matching algorithm is partial.
	 *
	 * @param hardwareId The (partial) hardware Id to search by.
	 */
	@GET
	@Path("/v1/find-devices/by-hardware-id")
	List<DeviceEntity> findDevicesByHardwareId(
		@QueryParam("hardwareId") String hardwareId);

	@DELETE
	@Path("/v1/{commandId}")
	void deleteCommand(@PathParam("commandId") String commandId);

	@DELETE
	@Path("/v1/reply/{replyId}")
	void deleteReply(@PathParam("replyId") String replyId);

	@DELETE
	@Path("/v1/reply/all/{correlationId}")
	void deleteReplies(@PathParam("correlationId") String correlationId);

	/**
	 * Purges all command and replies older than the given duration.
	 * @param durationInDays The duration, in days.
	 */
	@DELETE
	@Path("/v1/purge/{durationInDays}")
	void purge(@PathParam("durationInDays")int durationInDays);

	/**
	 * Purges all command and replies.
	 */
	@DELETE
	@Path("/v1/purge")
	void purge();

	@PUT
	@Path("/v1/replay/{sourceCommandId}")
	void replayCommand(@PathParam("sourceCommandId") String sourceCommandId);

}
