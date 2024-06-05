package esthesis.service.dt.resource;

import esthesis.service.dt.security.DTSecurityFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/api/dt")
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


	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/execute/sync")
	Response executeCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/execute/async")
	Response executeCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/execute/sync")
	Response executeCommandSyncByTag(@PathParam("tag") String tag, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/execute/async")
	Response executeCommandAsyncByTag(@PathParam("tag") String tag, String command);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/command/{correlationId}/reply")
	Response getCommandReply(@PathParam("correlationId") String correlationId);

}
