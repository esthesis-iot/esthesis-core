package esthesis.service.dt.resource;

import esthesis.service.dt.security.DTSecurityFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/dt")
@Tag(name = "DTResource")
public interface DTResource {

	@GET
	@DTSecurityFilter
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	@Produces("application/json")
	@Operation(summary = "Get specific measurement value for a given category and device in JSON format")
	Response findJSON(String hardwareId, String category, String measurement);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	@Produces("text/plain")
	@Operation(summary = "Get specific measurement value for a given category and device")
	Response findPlain(String hardwareId, String category,
		String measurement);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/{hardwareId}/{category}")
	@Produces("application/json")
	@Operation(summary = "Get all measurement values for a given category and device")
	Response findAllJSON(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/{hardwareId}/{category}")
	@Produces("text/plain")
	@Operation(summary = "Get all measurement values for a given category and device")
	Response findAllPlain(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Path("/v1/measurements/{hardwareId}/{category}")
	@Operation(summary = "Get all measurements for a given category and device")
	Response findMeasurements(String hardwareId, String category);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/execute/sync")
	@Operation(summary = "Send a custom execute command synchronously to a device")
	Response executeCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/execute/async")
	@Operation(summary = "Send a custom execute command asynchronously to a device")
	Response executeCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/execute/sync")
	@Operation(summary = "Send a custom execute command synchronously to devices by tag")
	Response executeCommandSyncByTag(@PathParam("tag") String tag, String command);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/execute/async")
	@Operation(summary = "Send a custom execute command asynchronously to devices by tag")
	Response executeCommandAsyncByTag(@PathParam("tag") String tag, String command);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/command/{correlationId}/reply")
	@Operation(summary = "Get replies for previously sent asynchronous command")
	Response getCommandReply(@PathParam("correlationId") String correlationId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/ping/sync")
	@Operation(summary = "Send a PING command synchronously to a device")
	Response pingCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/ping/async")
	@Operation(summary = "Send a PING command asynchronously to a device")
	Response pingCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/ping/sync")
	@Operation(summary = "Send a PING command synchronously to devices by tag")
	Response pingCommandSyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/ping/async")
	@Operation(summary = "Send a PING command asynchronously to devices by tag")
	Response pingCommandAsyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/shutdown/sync")
	@Operation(summary = "Send a SHUTDOWN command synchronously to a device")
	Response shutdownCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/shutdown/async")
	@Operation(summary = "Send a SHUTDOWN command asynchronously to a device")
	Response shutdownCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/shutdown/sync")
	@Operation(summary = "Send a SHUTDOWN command synchronously to devices by tag")
	Response shutdownCommandSyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/shutdown/async")
	@Operation(summary = "Send a SHUTDOWN command asynchronously to devices by tag")
	Response shutdownCommandAsyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/reboot/sync")
	@Operation(summary = "Send a REBOOT command synchronously to a device")
	Response rebootCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/reboot/async")
	@Operation(summary = "Send a REBOOT command asynchronously to a device")
	Response rebootCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/reboot/sync")
	@Operation(summary = "Send a REBOOT command synchronously to devices by tag")
	Response rebootCommandSyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/reboot/async")
	@Operation(summary = "Send a REBOOT command asynchronously to devices by tag")
	Response rebootCommandAsyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/firmware/sync")
	@Operation(summary = "Send a FIRMWARE UPDATE command synchronously to a device")
	Response firmwareCommandSyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/firmware/async")
	@Operation(summary = "Send a FIRMWARE UPDATE command asynchronously to a device")
	Response firmwareCommandAsyncByHardwareId(@PathParam("hardwareId") String hardwareId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/firmware/sync")
	@Operation(summary = "Send a FIRMWARE UPDATE command synchronously to devices by tag")
	Response firmwareCommandSyncByTag(@PathParam("tag") String tag);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/firmware/async")
	@Operation(summary = "Send a FIRMWARE UPDATE command asynchronously to devices by tag")
	Response firmwareCommandAsyncByTag(@PathParam("tag") String tag);

}
