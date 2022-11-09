package esthesis.service.device.resource;

import esthesis.service.device.dto.DeviceProfileFieldData;
import esthesis.service.device.dto.DeviceProfileNote;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceProfileResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceProfileResource {

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

  @GET
  @Path("/v1/device/{deviceId}/device-data")
  List<DeviceProfileFieldData> getAllDeviceData(String deviceId);

}
