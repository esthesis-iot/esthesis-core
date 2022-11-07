package esthesis.service.device.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceProfileFieldData;
import esthesis.service.device.dto.DeviceProfileNote;
import esthesis.service.device.dto.DeviceRegistration;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
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
  @Path("/v1/device/activate/{hardwareId}")
  Device activatePreregisteredDevice(
      @PathParam(value = "hardwareId") String hardwareId);

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
  @Path("/v1/device/{deviceId}/keys")
  @SuppressWarnings("java:S1192")
  Response downloadKeys(@PathParam("deviceId") ObjectId deviceId);

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

  @GET
  @Path("/v1/device/{deviceId}/device-profile/")
  List<DeviceProfileNote> getDeviceProfileNotes(
      @PathParam("deviceId") String deviceId);

  @POST
  @Path("/v1/device/{deviceId}/device-profile")
  List<DeviceProfileNote> saveDeviceProfileNotes(Map<String, String> fields,
      @PathParam("deviceId") String deviceId);

  @POST
  @Path("/v1/device/device-profile/add-note")
  DeviceProfileNote addDeviceProfileNote(DeviceProfileNote field);

  @DELETE
  @Path("/v1/device/{deviceId}/device-profile/delete-note")
  void deleteDeviceProfileNote(@PathParam("deviceId") String deviceId,
      @QueryParam("keyName") String keyName);

  @GET
  @Path("/v1/device/{deviceId}/device-profile/fields-data")
  List<DeviceProfileFieldData> getFieldsData(String deviceId);
}
