package esthesis.service.dt.resource;

import esthesis.service.dt.security.DTSecurityFilter;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/dt")
@Tag(name = "DTResource")
public interface DTResource {

	@GET
	@DTSecurityFilter
	@Produces("application/json")
	@Operation(summary = "Get specific measurement value for a given category and device in JSON format")
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	Response findJSON(String hardwareId, String category, String measurement);

	@GET
	@DTSecurityFilter
	@Produces("text/plain")
	@Operation(summary = "Get specific measurement value for a given category and device")
	@Path("/v1/get/{hardwareId}/{category}/{measurement}")
	Response findPlain(String hardwareId, String category,
										 String measurement);

	@GET
	@DTSecurityFilter
	@Produces("application/json")
	@Operation(summary = "Get all measurement values for a given category and device")
	@Path("/v1/get/{hardwareId}/{category}")
	Response findAllJSON(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Produces("text/plain")
	@Operation(summary = "Get all measurement values for a given category and device")
	@Path("/v1/get/{hardwareId}/{category}")
	Response findAllPlain(String hardwareId, String category);

	@GET
	@DTSecurityFilter
	@Path("/v1/measurements/{hardwareId}/{category}")
	@Operation(summary = "Get all measurements for a given category and device")
	Response findMeasurements(String hardwareId, String category);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/execute")
	@Operation(summary = "Send a custom execute command to a device, either synchronously (default) or asynchronously")
	Response executeCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "The command to execute on the device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		String command,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/execute")
	@Operation(summary = "Send a custom command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response executeCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "The command to execute on the devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		String command,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@GET
	@DTSecurityFilter
	@Path("/v1/get/command/{correlationId}/reply")
	@Operation(summary = "Get replies for previously sent asynchronous command")
	Response getCommandReply(@PathParam("correlationId") String correlationId);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/ping")
	@Operation(summary = "Send a ping command to a device, either synchronously (default) or asynchronously")
	Response pingCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/ping")
	@Operation(summary = "Send a ping command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response pingCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/shutdown")
	@Operation(summary = "Send a shutdown command to a device, either synchronously (default) or asynchronously")
	Response shutdownCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/shutdown")
	@Operation(summary = "Send a shutdown command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response shutdownCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/reboot")
	@Operation(summary = "Send a reboot command to a device, either synchronously (default) or asynchronously")
	Response rebootCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/reboot")
	@Operation(summary = "Send a reboot command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response rebootCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/firmware")
	@Operation(summary = "Send a firmware update command to a device, either synchronously (default) or asynchronously")
	Response firmwareCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/firmware")
	@Operation(summary = "Send a firmware update command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response firmwareCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/device/{hardwareId}/health")
	@Operation(summary = "Send a health command to a device, either synchronously (default) or asynchronously")
	Response healthCommandByHardwareId(
		@Parameter(
			description = "The hardware ID of the target device",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("hardwareId") String hardwareId,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

	@POST
	@DTSecurityFilter
	@Path("/v1/command/tag/{tag}/health")
	@Operation(summary = "Send a health command to be executed on devices by tag, either synchronously (default) or asynchronously")
	Response healthCommandByTag(
		@Parameter(
			description = "The tag of the targeted devices",
			required = true,
			schema = @Schema(type = SchemaType.STRING))
		@PathParam("tag") String tag,

		@Parameter(description = "Set to true to execute the command asynchronously",
			schema = @Schema(type = SchemaType.BOOLEAN))
		@QueryParam("async") @DefaultValue("false") boolean async
	);

}
