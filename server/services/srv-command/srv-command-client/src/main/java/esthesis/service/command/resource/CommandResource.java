package esthesis.service.command.resource;

import esthesis.common.dto.CommandReply;
import esthesis.service.command.dto.CommandRequest;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "CommandResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CommandResource {

  @GET
  @Path("/v1/command/find")
  Page<CommandRequest> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/command/{commandId}")
  CommandRequest getCommand(@PathParam("commandId") String commandId);

  @GET
  @Path("/v1/command/reply/{correlationId}")
  List<CommandReply> getReply(@PathParam("correlationId") String correlationId);

  /**
   * Saves and executed a command request and directly returns the correlation
   * ID, so the results of this command can be obtained later.
   *
   * @param request The command request to save and execute.
   * @return The correlation ID of the command request.
   */
  @POST
  @Path("/v1/command")
  String save(CommandRequest request);

  /**
   * Saves and executed a command request and waits until the reply for that
   * command is available to return it. Since a command may target multiple
   * devices, the result is a list of replies. The amount of time to wait before
   * declaring a timeout is configurable.
   *
   * @param request      The command request to save and execute.
   * @param timeout      The amount of time (in milliseconds) to wait for
   *                     obtaining the results before declaring a timeout. By
   *                     default, the timeout is set at 3 seconds.
   * @param pollInterval The amount of time (in milliseconds) to wait before
   *                     checking if the results are available. By default, the
   *                     poll interval is set at 500 milliseconds. A smaller
   *                     pollInterval makes the device seem more responsive,
   *                     however it may introduce extra load to the database.
   * @return The correlation ID of the command request.
   */

  @POST
  @Path("/v1/command/wait-for-reply")
  List<CommandReply> saveAndWait(CommandRequest request,
      @QueryParam("timeout") @DefaultValue("3000") long timeout,
      @QueryParam("pollInterval") @DefaultValue("500") long pollInterval);

  /**
   * Counts the number of devices with the given hardware IDs. The matching
   * algorithm is partial.
   *
   * @param hardwareIds A comma-separated list of hardware IDs.
   */
  @GET
  @Path("/v1/command/count-devices/by-hardware-id")
  Long countDevicesByHardwareIds(
      @QueryParam("hardwareIds") String hardwareIds);

  /**
   * Counts the number of devices with the given tags. The matching algorithm is
   * partial.
   *
   * @param tags A comma-separated list of tag names.
   */
  @GET
  @Path("/v1/command/count-devices/by-tags")
  Long countDevicesByTags(@QueryParam("tags") String tags);

  @DELETE
  @Path("/v1/command/{commandId}")
  void deleteCommand(@PathParam("commandId") String commandId);

  @DELETE
  @Path("/v1/command/reply/{replyId}")
  void deleteReply(@PathParam("replyId") String replyId);

  @DELETE
  @Path("/v1/command/purge/{durationInDays}")
  void purge(
      @PathParam("durationInDays") @DefaultValue("0") Optional<Integer> durationInDays);

  @DELETE
  @Path("/v1/command/purge")
  void purgeAll();
}
