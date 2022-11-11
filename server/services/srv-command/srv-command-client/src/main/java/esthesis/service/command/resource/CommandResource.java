package esthesis.service.command.resource;

import esthesis.common.dto.CommandReply;
import esthesis.common.dto.CommandRequest;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "CommandResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CommandResource {

  @POST
  @Path("/v1/command/request")
  String dispatch(CommandRequest request);

  @PUT
  @Path("/v1/command/request")
  CommandReply dispatchAndWait(CommandRequest request);

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

}
