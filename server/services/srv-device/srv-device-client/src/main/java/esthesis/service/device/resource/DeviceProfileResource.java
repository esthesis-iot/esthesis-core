package esthesis.service.device.resource;

import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.entity.DeviceProfileNoteEntity;
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
  List<DeviceProfileNoteEntity> getDeviceProfileNotes(
      @PathParam("deviceId") String deviceId);

  @POST
  @Path("/v1/device/{deviceId}/device-profile")
  List<DeviceProfileNoteEntity> saveDeviceProfileNotes(Map<String, String> fields,
      @PathParam("deviceId") String deviceId);

  @POST
  @Path("/v1/device/device-profile/add-note")
  DeviceProfileNoteEntity addDeviceProfileNote(DeviceProfileNoteEntity field);

  @DELETE
  @Path("/v1/device/{deviceId}/device-profile/delete-note")
  void deleteDeviceProfileNote(@PathParam("deviceId") String deviceId,
      @QueryParam("keyName") String keyName);

  @GET
  @Path("/v1/device/{deviceId}/device-profile/fields-data")
  List<DeviceProfileFieldDataDTO> getFieldsData(String deviceId);

  @GET
  @Path("/v1/device/{deviceId}/device-data")
  List<DeviceProfileFieldDataDTO> getAllDeviceData(String deviceId);

}
