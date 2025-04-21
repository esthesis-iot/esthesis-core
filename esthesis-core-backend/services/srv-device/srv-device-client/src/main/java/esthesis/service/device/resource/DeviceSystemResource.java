package esthesis.service.device.resource;

import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.dto.DevicesTotalsStatsDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the device system resource when accessed by a SYSTEM OIDC client.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "DeviceSystemResource")
public interface DeviceSystemResource {

	/**
	 * Registers a new device.
	 *
	 * @param deviceRegistration The device registration data.
	 * @return The registered device entity.
	 * @throws IOException               If an I/O error occurs.
	 * @throws InvalidKeySpecException   If the public key is invalid.
	 * @throws NoSuchAlgorithmException  If the public key algorithm is not supported.
	 * @throws OperatorCreationException If the public key operator cannot be created.
	 * @throws NoSuchProviderException   If the public key provider is not found.
	 */
	@POST
	@Path("/v1/system/register")
	DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
				 OperatorCreationException, NoSuchProviderException;

	/**
	 * Finds a device by its hardware ID.
	 *
	 * @param hardwareId The hardware ID of the device.
	 * @return The device entity, or null if not found.
	 */
	@GET
	@Path("/v1/system/find/by-hardware-id")
	DeviceEntity findByHardwareId(@QueryParam("hardwareId") String hardwareId);

	/**
	 * Find a device by its esthesis ID.
	 *
	 * @param esthesisId The esthesis ID of the device.
	 * @return The device entity, or null if not found.
	 */
	@GET
	@Path("/v1/system/find/by-id")
	DeviceEntity findById(@QueryParam("esthesisId") String esthesisId);

	/**
	 * Finds the public key of a device.
	 *
	 * @param hardwareId The hardware ID of the device.
	 * @return The public key of the device.
	 */
	@GET
	@Path("/v1/system/public-key")
	String findPublicKey(@QueryParam("hardwareId") String hardwareId);

	/**
	 * Finds the device attributes by esthesis ID.
	 *
	 * @param esthesisId The esthesis ID of the device.
	 * @return The list of device attributes.
	 */
	@GET
	@Path("/v1/system/{esthesisId}/attributes-by-esthesis-id")
	List<DeviceAttributeEntity> getDeviceAttributesByEsthesisId(
		@PathParam("esthesisId") String esthesisId);

	/**
	 * Finds the device attributes by the device hardware ID.
	 *
	 * @param esthesisHardwareId The hardware ID of the device.
	 * @return The list of device attributes.
	 */
	@GET
	@Path("/v1/system/{esthesisHardwareId}/attributes-by-esthesis-hardware-id")
	List<DeviceAttributeEntity> getDeviceAttributesByEsthesisHardwareId(
		@PathParam("esthesisHardwareId") String esthesisHardwareId);

	/**
	 * Gets the device IDs.
	 *
	 * @return The list of device IDs.
	 */
	@GET
	@Path("/v1/system/device-ids")
	List<String> getDeviceIds();

	/**
	 * Gets the device attribute by esthesis hardware ID and attribute name.
	 *
	 * @param esthesisHardwareId The hardware ID of the device.
	 * @param attributeName      The name of the attribute.
	 * @return The device attribute entity, or null if not found.
	 */
	@GET
	@Path("/v1/system/{esthesisHardwareId}/attribute-by-esthesis-id/{attributeName}")
	Optional<DeviceAttributeEntity> getDeviceAttributeByEsthesisHardwareIdAndAttributeName(
		@PathParam("esthesisHardwareId") String esthesisHardwareId,
		@PathParam("attributeName") String attributeName);

	/**
	 * Get statistics on a device.
	 *
	 * @return The device statistics.
	 */
	@GET
	@Path("/v1/system/device-stats")
	DevicesLastSeenStatsDTO getDeviceStats();

	/**
	 * Get statistics on all devices.
	 *
	 * @return The device statistics.
	 */
	@GET
	@Path("/v1/system/device-totals")
	DevicesTotalsStatsDTO getDeviceTotalsStats();

	/**
	 * Get the latest devices.
	 *
	 * @param limit The maximum number of devices to return.
	 * @return The list of latest devices.
	 */
	@GET
	@Path("/v1/system/device-latest")
	List<DeviceEntity> getLatestDevices(@QueryParam("limit") Integer limit);

	/**
	 * Find devices by tag name.
	 *
	 * @param tags Comma-separated list of tag names.
	 * @return List of device hardware IDs.
	 */
	@GET
	@Path("/v1/find/by-tag-names")
	List<String> findByTagNames(@QueryParam("tags") String tags);

	/**
	 * Find devices by tag IDs.
	 *
	 * @param tags Comma-separated list of tag IDs.
	 * @return List of device hardware IDs.
	 */
	@GET
	@Path("/v1/find/by-tag-ids")
	List<String> findByTagIds(@QueryParam("tags") String tags);
}
