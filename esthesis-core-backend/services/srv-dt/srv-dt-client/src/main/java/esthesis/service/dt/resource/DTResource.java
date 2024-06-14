package esthesis.service.dt.resource;

import esthesis.service.dt.security.DTSecurityFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/api")
public interface DTResource {

	@GET
	@DTSecurityFilter
	@Produces("application/json")
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	Response findJSON(String hardwareId, String category, String measurement);

	@GET
	@DTSecurityFilter
	@Produces("text/plain")
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	Response findPlain(String hardwareId, String category,
		String measurement);

	@GET
	@DTSecurityFilter
	@Produces("application/json")
	@Path("/v1/get/{hardwareId}/{category}")
	Response findAllJSON(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Produces("text/plain")
	@Path("/v1/get/{hardwareId}/{category}")
	Response findAllPlain(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Path("/v1/measurements/{hardwareId}/{category}")
	Response findMeasurements(String hardwareId, String category);

}
