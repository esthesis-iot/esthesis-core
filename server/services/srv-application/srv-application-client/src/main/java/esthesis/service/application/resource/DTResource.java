package esthesis.service.application.resource;

import esthesis.service.application.dto.DTValueReply;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DTResource")
//@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DTResource {

  @GET
  @Path("/v1/dt/get/{hardwareId}/{category}/{measurement}")
  DTValueReply find(String hardwareId, String category, String measurement);

}
