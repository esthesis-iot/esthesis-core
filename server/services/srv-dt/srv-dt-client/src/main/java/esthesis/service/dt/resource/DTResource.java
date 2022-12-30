package esthesis.service.dt.resource;

import esthesis.service.dt.security.DTSecurityFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/api")
public interface DTResource {

  @GET
  @DTSecurityFilter
  @Path("/v1/get/{hardwareId}/{category}/{measurement}")
  @Produces("application/json")
  Response findJSON(String hardwareId, String category, String measurement);

  @GET
  @DTSecurityFilter
  @Path("/v1/get/{hardwareId}/{category}/{measurement}")
  @Produces("text/plain")
  Response findPlain(String hardwareId, String category,
      String measurement);

  @GET
  @DTSecurityFilter
  @Path("/v1/get/{hardwareId}/{category}")
  @Produces("application/json")
  Response findAllJSON(String hardwareId, String category);

  @GET
  @DTSecurityFilter
  @Path("/v1/get/{hardwareId}/{category}")
  @Produces("text/plain")
  Response findAllPlain(String hardwareId, String category);

  @GET
  @DTSecurityFilter
  @Path("/v1/measurements/{hardwareId}/{category}")
  Response findMeasurements(String hardwareId, String category);

}
