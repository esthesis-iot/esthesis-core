package esthesis.service.application.resource;

import esthesis.service.application.dto.DTValueReplyDTO;
import esthesis.service.application.security.DTSecurityFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/api")
public interface DTResource {

  @GET
  @DTSecurityFilter
  @Path("/v1/dt/get/{hardwareId}/{category}/{measurement}")
  @Produces("application/json")
  DTValueReplyDTO findJSON(String hardwareId, String category, String measurement);

  @GET
  @DTSecurityFilter
  @Path("/v1/dt/get/{hardwareId}/{category}/{measurement}")
  @Produces("text/plain")
  String findPlain(String hardwareId, String category,
      String measurement);

  @GET
  @DTSecurityFilter
  @Path("/v1/dt/get/{hardwareId}/{category}")
  @Produces("application/json")
  List<DTValueReplyDTO> findAllJSON(String hardwareId, String category);

  @GET
  @DTSecurityFilter
  @Path("/v1/dt/get/{hardwareId}/{category}")
  @Produces("text/plain")
  String findAllPlain(String hardwareId, String category);

  @GET
  @DTSecurityFilter
  @Path("/v1/dt/measurements/{hardwareId}/{category}")
  String findMeasurements(String hardwareId, String category);

}
