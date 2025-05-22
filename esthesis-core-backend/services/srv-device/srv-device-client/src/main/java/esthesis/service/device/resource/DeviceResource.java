package esthesis.service.device.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Device.DataImportType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceDataImportDTO;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.dto.DeviceTextDataImportDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * REST client for the device resource.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DeviceResource")
public interface DeviceResource {

	/**
	 * Counts the number of devices having one or more of the given tag names.
	 *
	 * @param tags A comma-separated list of tag names to search by.
	 * @return The number of devices having one or more of the given tag names.
	 */
	@GET
	@Path("/v1/count/by-tag")
	Long countByTags(@QueryParam("tag") String tags);

	@GET
	@Path("/v1/count/by-status")
	Long countByStatus(@QueryParam("status") AppConstants.Device.Status status);

	/**
	 * Counts the number of devices having one or more of the given hardware IDs.
	 *
	 * @param hardwareIds A comma-separated list of hardware IDs to search by.
	 * @return The number of devices having one or more of the given hardware IDs.
	 */
	@GET
	@Path("/v1/count/by-hardware-id")
	Long countByHardwareIds(@QueryParam("hardwareIds") String hardwareIds);

	/**
	 * Finds the devices matching a comma-separated list of hardware IDs.
	 *
	 * @param hardwareIds The hardware IDs to search by.
	 * @return The list of devices matching the given hardware IDs.
	 */
	@GET
	@Path("/v1/find/by-hardware-id")
	List<DeviceEntity> findByHardwareIds(@QueryParam("hardwareIds") String hardwareIds);

	/**
	 * Finds the devices matching the given tag names.
	 *
	 * @param tag The tag name to search by.
	 * @return The list of devices matching the given tag name.
	 */
	@GET
	@Path("/v1/find/by-tag-name")
	List<DeviceEntity> findByTagName(@QueryParam("tag") String tag);

	/**
	 * Finds the devices matching the given tag IDs.
	 *
	 * @param tagId The tag ID to search by.
	 * @return The list of devices matching the given tag ID.
	 */
	@GET
	@Path("/v1/find/by-tag-id")
	List<DeviceEntity> findByTagId(@QueryParam("tag") String tagId);

	/**
	 * List devices.
	 *
	 * @param pageable pagination parameters.
	 * @return a page of devices.
	 */
	@GET
	@Path("/v1/find")
	Page<DeviceEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds the devices by the given hardware ID.
	 *
	 * @param id The hardware ID to search by.
	 * @return The device matching the given hardware ID.
	 */
	@GET
	@Path("/v1/{deviceId}")
	DeviceEntity get(@PathParam("deviceId") String id);

	/**
	 * Deletes the device with the given ID.
	 *
	 * @param id The ID of the device to delete.
	 */
	@DELETE
	@Path("/v1/{deviceId}")
	void delete(@PathParam("deviceId") String id);

	/**
	 * Gets the geolocation information of the device with the given ID.
	 *
	 * @param deviceId The ID of the device to get the geolocation information for.
	 * @return The geolocation information of the device.
	 */
	@GET
	@Path("/v1/{deviceId}/geolocation")
	GeolocationDTO getDeviceGeolocation(String deviceId);

	/**
	 * Downloads the key of the given type for the device with the given ID.
	 *
	 * @param deviceId The ID of the device to download the key for.
	 * @param type     The type of the key to download.
	 * @return The key of the given type for the device with the given ID.
	 */
	@GET
	@Path("/v1/{deviceId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("deviceId") String deviceId,
		@QueryParam("type") AppConstants.KeyType type);

	/**
	 * Gets the profile of the device with the given ID.
	 *
	 * @param deviceId The ID of the device to get the profile for.
	 * @return The profile of the device.
	 */
	@GET
	@Path("/v1/{deviceId}/profile")
	DeviceProfileDTO getProfile(@PathParam("deviceId") String deviceId);

	/**
	 * Saves the profile of the device with the given ID.
	 *
	 * @param deviceId         The ID of the device to save the profile for.
	 * @param deviceProfileDTO The profile to save.
	 */
	@POST
	@Path("/v1/{deviceId}/profile")
	void saveProfile(@PathParam("deviceId") String deviceId, DeviceProfileDTO deviceProfileDTO);

	/**
	 * Imports device data from text.
	 *
	 * @param deviceId                The ID of the device to import data for.
	 * @param type                    The type of the data to import.
	 * @param deviceTextDataImportDTO The data to import.
	 */
	@POST
	@Path("/v1/{deviceId}/import-data/{type}/text")
	void importDeviceDataFromText(@PathParam("deviceId") @NotEmpty String deviceId,
		@PathParam("type") @NotNull DataImportType type,
		@NotNull DeviceTextDataImportDTO deviceTextDataImportDTO);

	/**
	 * Imports device data from a file.
	 *
	 * @param deviceId            The ID of the device to import data for.
	 * @param type                The type of the data to import.
	 * @param deviceDataImportDTO The data to import.
	 * @param file                The file to import the data from.
	 */
	@POST
	@Path("/v1/{deviceId}/import-data/{type}/file")
	void importDeviceDataFromFile(@PathParam("deviceId") @NotEmpty String deviceId,
		@PathParam("type") @NotNull DataImportType type,
		@RestForm("dto") @PartType(MediaType.APPLICATION_JSON) @NotNull DeviceDataImportDTO deviceDataImportDTO,
		@RestForm("file") @NotNull FileUpload file);

	/**
	 * Returns all data (i.e. metrics) available in cache for this device.
	 *
	 * @param deviceId The device ID.
	 * @return The list of device data.
	 */
	@GET
	@Path("/v1/{deviceId}/device-data")
	List<DeviceProfileFieldDataDTO> getDeviceData(@PathParam("deviceId") String deviceId);

	/**
	 * Preregisters a device.
	 *
	 * @param deviceRegistration The device registration information.
	 * @return The device entities that were preregistered.
	 * @throws NoSuchAlgorithmException  when the algorithm is not found.
	 * @throws IOException               when an I/O exception occurs.
	 * @throws OperatorCreationException when the operator cannot be created.
	 * @throws InvalidKeySpecException   when the key specification is invalid.
	 * @throws NoSuchProviderException   when the provider is not found.
	 */
	@POST
	@Path("/v1/preregister")
	List<DeviceEntity> preregister(@Valid DeviceRegistrationDTO deviceRegistration)
	throws NoSuchAlgorithmException, IOException, OperatorCreationException,
				 InvalidKeySpecException, NoSuchProviderException;

	/**
	 * Activates a preregistered device.
	 *
	 * @param hardwareId The hardware ID of the device to activate.
	 * @return The device entity.
	 */
	@PUT
	@Path("/v1/activate/{hardwareId}")
	DeviceEntity activatePreregisteredDevice(@PathParam(value = "hardwareId") String hardwareId);

	@POST
	@Path("/v1/tags-and-status")
	Response saveTagsAndStatus(DeviceEntity deviceEntity);
}
