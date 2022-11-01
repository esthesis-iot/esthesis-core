package esthesis.service.application.resource;

import esthesis.service.application.dto.DTValueReply;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DTResource")
//@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DTResource {

  @GET
  @Path("/v1/dt/get/{hardwareId}/{category}/{measurement}")
  @Produces("application/json")
  DTValueReply findJSON(String hardwareId, String category, String measurement);

  @GET
  @Path("/v1/dt/get/{hardwareId}/{category}/{measurement}")
  @Produces("text/plain")
  String findPlain(String hardwareId, String category,
      String measurement);

  @GET
  @Path("/v1/dt/get/{hardwareId}/{category}")
  @Produces("application/json")
  List<DTValueReply> findAllJSON(String hardwareId, String category);

  @GET
  @Path("/v1/dt/get/{hardwareId}/{category}")
  @Produces("text/plain")
  String findAllPlain(String hardwareId, String category);

  @GET
  @Path("/v1/dt/measurements/{hardwareId}/{category}")
  String findMeasurements(String hardwareId, String category);
  
}
