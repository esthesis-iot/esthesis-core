package esthesis.service.device.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DevicePage;
import esthesis.service.device.dto.DeviceRegistration;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceResource {

  @POST
  @Path("/v1/device/preregister")
  Response preregister(@Valid DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException;

  @PUT
  @Path("/v1/device/activate/{id}")
  Device activatePreregisteredDevice(
      @PathParam(value = "id") String hardwareId);

  @GET
  @Path("/v1/device/find")
  Page<Device> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/device/{id}")
  Device get(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/device/{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/device")
  Device save(@Valid Device object);

  @GET
  @Path("/v1/device/{deviceId}/keys")
  @SuppressWarnings("java:S1192")
  Response downloadKeys(@PathParam("deviceId") ObjectId deviceId);

  @GET
  @Path("/v1/device/device-page-data/{deviceId}")
  List<DevicePage> getDevicePageData(@PathParam("deviceId") long deviceId);

  /**
   * Returns the last value of a specific telemetry or metadata field for a
   * device.
   *
   * @param deviceId The Id of the device to fetch the field value for.
   * @param fields   The name of the telemetry or metadata field to fetch. The
   *                 field needs to follow the following format:
   *                 TYPE.MEASUREMENT.FIELD For example,
   *                 TELEMETRY.geolocation.latitude. Multiple fields can be
   *                 requested separated by comma.
   */
  @GET
  @Path("/v1/device/device-data-field/{deviceId}")
  List<DevicePage> getDeviceDataFields(@PathParam("deviceId") long deviceId,
      @QueryParam("fields") String fields);

  @GET
  @Path("/v1/device/count/by-hardware-id")
  int countByHardwareId(@QueryParam("hardwareIds") String hardwareIds);

  @GET
  @Path("/v1/device/count/by-tags")
  int countByTags(@QueryParam("tags") String tags);

  @GET
  @Path("/v1/device/by-partial-hardware-id/{hardwareId}")
  List<Device> findByPartialHardwareId(
      @PathParam("hardwareId") String hardwareId);

}
