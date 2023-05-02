package esthesis.service.device.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
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

	@GET
	@Path("/v1/find/by-tag-id")
	List<DeviceEntity> findByTagId(@QueryParam("tag") String tagId);

	@GET
	@Path("/v1/find")
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
	void saveProfile(@PathParam("deviceId") String deviceId,
		DeviceProfileDTO deviceProfileDTO);

//  @POST
//  @Path("/v1/{deviceId}/attribute")
//  DeviceAttributeEntity addDeviceAttribute(@PathParam("deviceId") String deviceId,
//      DeviceAttributeEntity field);

//  @DELETE
//  @Path("/v1/{deviceId}/attribute")
//  void deleteDeviceAttribute(@PathParam("deviceId") String deviceId,
//      @QueryParam("keyName") String keyName);

	/**
	 * Returns the user-defined (in settings) fields (i.e. metrics) for this device.
	 *
	 * @param deviceId
	 * @return
	 */
//  @GET
//  @Path("/v1/{deviceId}/fields")
//  List<DeviceProfileFieldDataDTO> getDeviceFields(String deviceId);

	/**
	 * Returns all data (i.e. metrics) available in cache for this device.
	 *
	 * @param deviceId
	 * @return
	 */
	@GET
	@Path("/v1/{deviceId}/device-data")
	List<DeviceProfileFieldDataDTO> getDeviceData(String deviceId);

}
