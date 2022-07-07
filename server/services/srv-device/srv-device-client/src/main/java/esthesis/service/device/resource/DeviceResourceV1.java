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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/device")
@RegisterRestClient(configKey = "DeviceResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceResourceV1 {

  @POST
  @Path("preregister")
  Response preregister(@Valid DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException;

  @GET
  Page<Device> find(Pageable pageable);

  @GET
  @Path("{id}")
  Device get(@PathParam("id") ObjectId id);

  @DELETE
  @Path("{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST
  Device save(@Valid Device object);

  @GET
  @Path("{deviceId}/keys")
  @SuppressWarnings("java:S1192")
  Response downloadKeys(@PathParam("deviceId") ObjectId deviceId);

  @GET
  @Path("device-page-data/{deviceId}")
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
  @Path("device-data-field/{deviceId}")
  List<DevicePage> getDeviceDataFields(@PathParam("deviceId") long deviceId,
      @QueryParam("fields") String fields);

  @GET
  @Path("count/by-hardware-id")
  int countByHardwareId(@QueryParam("hardwareIds") String hardwareIds);

  @GET
  @Path("count/by-tags")
  int countByTags(@QueryParam("tags") String tags);

  @GET
  @Path("/by-partial-hardware-id/{hardwareId}")
  List<Device> findByPartialHardwareId(
      @PathParam("hardwareId") String hardwareId);

}
