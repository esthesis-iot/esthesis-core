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
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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
 * Interface and REST client for the DeviceResource.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DeviceResource")
public interface DeviceResource {

	/**
	 * Counts the number of devices having one or more of the given tag names.
	 *
	 * @param tags         A comma-separated list of tag names to search by.
	 * @param partialMatch If true, the search will be performed using partial matching.
	 */
	@GET
	@Path("/v1/count/by-tag")
	Long countByTags(@QueryParam("tag") String tags,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	/**
	 * Counts the number of devices having one or more of the given hardware IDs.
	 *
	 * @param hardwareIds  A comma-separated list of hardware IDs to search by.
	 * @param partialMatch Whether to perform a partial match on the hardware IDs.
	 */
	@GET
	@Path("/v1/count/by-hardware-id")
	Long countByHardwareIds(@QueryParam("hardwareIds") String hardwareIds,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	/**
	 * Finds the devices matching a comma-separated list of hardware IDs.
	 *
	 * @param hardwareIds  The hardware IDs to search by.
	 * @param partialMatch Whether to perform a partial match on the hardware IDs.
	 */
	@GET
	@Path("/v1/find/by-hardware-id")
	List<DeviceEntity> findByHardwareIds(@QueryParam("hardwareIds") String hardwareIds,
		@QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

	/**
	 * Finds the devices matching the given tag names.
	 *
	 * @param tag The tag name to search by.
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

	@GET
	@Path("/v1/find")
	@RolesAllowed("default-roles-esthesis")
	Page<DeviceEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{deviceId}")
	DeviceEntity get(@PathParam("deviceId") String id);

	@DELETE
	@Path("/v1/{deviceId}")
	void delete(@PathParam("deviceId") String id);

	@POST
	@Path("/v1")
	DeviceEntity save(@Valid DeviceEntity object);

	@GET
	@Path("/v1/{deviceId}/geolocation")
	GeolocationDTO getDeviceGeolocation(String deviceId);

	@GET
	@Path("/v1/{deviceId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("deviceId") String deviceId,
		@QueryParam("type") AppConstants.KeyType type);


	@GET
	@Path("/v1/{deviceId}/profile")
	DeviceProfileDTO getProfile(@PathParam("deviceId") String deviceId);

	@POST
	@Path("/v1/{deviceId}/profile")
	void saveProfile(@PathParam("deviceId") String deviceId, DeviceProfileDTO deviceProfileDTO);

	@POST
	@Path("/v1/{deviceId}/import-data/{type}/text")
	void importDeviceDataFromText(@PathParam("deviceId") @NotEmpty String deviceId,
		@PathParam("type") @NotNull DataImportType type,
		@NotNull DeviceTextDataImportDTO deviceTextDataImportDTO);

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

	@POST
	@Path("/v1/preregister")
	Response preregister(@Valid DeviceRegistrationDTO deviceRegistration)
	throws NoSuchAlgorithmException, IOException, OperatorCreationException,
				 InvalidKeySpecException, NoSuchProviderException;

	@PUT
	@Path("/v1/activate/{hardwareId}")
	DeviceEntity activatePreregisteredDevice(@PathParam(value = "hardwareId") String hardwareId);
}
