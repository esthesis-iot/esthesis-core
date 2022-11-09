package esthesis.service.device.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.GeolocationDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.bson.types.ObjectId;
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
   * @param partialMatch If true, the search will be performed using partial
   *                     matching.
   */
  @GET
  @Path("/v1/device/count/by-tag")
  Long countByTags(@QueryParam("tag") String tags,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  /**
   * Counts the number of devices having one or more of the given hardware IDs.
   *
   * @param hardwareIds  A comma-separated list of hardware IDs to search by.
   * @param partialMatch Whether to perform a partial match on the hardware
   *                     IDs.
   */
  @GET
  @Path("/v1/device/count/by-hardware-id")
  Long countByHardwareIds(@QueryParam("hardwareIds") String hardwareIds,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  @GET
  @Path("/v1/device/find")
  Page<Device> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/device/{deviceId}")
  Device get(@PathParam("deviceId") ObjectId id);

  @DELETE
  @Path("/v1/device/{deviceId}")
  void delete(@PathParam("deviceId") ObjectId id);

  @POST
  @Path("/v1/device")
  Device save(@Valid Device object);

  @GET
  @Path("/v1/device/{deviceId}/geolocation")
  GeolocationDTO getDeviceGeolocation(String deviceId);
}
