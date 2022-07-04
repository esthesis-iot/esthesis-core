package esthesis.services.device.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.service.rest.Page;
import esthesis.common.service.rest.Pageable;
import esthesis.common.util.exception.QMismatchException;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceKey;
import esthesis.service.device.dto.DevicePage;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/device")
@RequestScoped
public class DeviceResource {

  @Inject
  DeviceService deviceService;

  @Inject
  JsonWebToken jwt;

  @POST
  @Path("preregister")
  public Response preregister(@Valid DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
    deviceService.preregister(deviceRegistration);

    return Response.ok().build();
  }

  @GET
  //@ReplyPageableFilter("createdOn,hardwareId,id,state,lastSeen")
  public Page<Device> findAll(Pageable pageable) {
    return deviceService.find(pageable);
  }

  @GET
  @Path("{id}")
//  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public Device get(@PathParam("id") ObjectId id) {
    return deviceService.findById(id, true);
  }

  @DELETE
  @Path("{id}")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete device.")
  public void delete(@PathParam("id") ObjectId id) {
    deviceService.deleteById(id);
  }

  @POST
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Device.")
  public Device save(@Valid Device object) {
    return deviceService.save(object);
  }

  @GET
  @Path("{deviceId}/keys")
  @SuppressWarnings("java:S1192")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device keys.")
  public Response downloadKeys(@PathParam("deviceId") ObjectId deviceId) {
    // Prepare a filename for downloading.
    String filename =
        Slugify.builder().build()
            .slugify(deviceService.findById(deviceId, true).getHardwareId())
            + ".keys";

    //TODO update with remaining keys for the device (i.e. provisioning)

    // Get the keys and decrypt values.
    final DeviceKey deviceKeys = deviceService.findKeys(deviceId);
    deviceKeys
        .setPrivateKey(deviceKeys.getPrivateKey());

    // Prepare the reply.
    // TODO switch to a JSON reply
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(
        "****************************************************************\n");
    stringBuilder.append("PUBLIC KEY\n");
    stringBuilder.append(
        "****************************************************************\n");
    stringBuilder.append(deviceKeys.getPublicKey());
    stringBuilder.append("\n");
    stringBuilder.append(
        "****************************************************************\n");
    stringBuilder.append("PRIVATE KEY\n");
    stringBuilder.append(
        "****************************************************************\n");
    stringBuilder.append(deviceKeys.getPrivateKey());
    stringBuilder.append("\n");
    // TODO we don't need the certificate too?

    return Response
        .ok(stringBuilder.toString())
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + filename)
        .type(MediaType.APPLICATION_OCTET_STREAM)
        .build();
  }

  @GET
  @Path("device-page-data/{deviceId}")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//      logMessage = "Could not fetch device page fields.")
//  @ReplyFilter("-shown,-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DevicePage> getDevicePageData(
      @PathParam("deviceId") long deviceId) {
    return deviceService.getDevicePageData(deviceId);
  }

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
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//      logMessage = "Could not fetch device page field.")
//  @ReplyFilter("-shown,-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DevicePage> getDeviceDataFields(
      @PathParam("deviceId") long deviceId,
      @QueryParam("fields") String fields) {
    List<DevicePage> results = new ArrayList<>();
    for (String field : fields.split(",")) {
      if (field.split("\\.").length < 3) {
        throw new QMismatchException("Unsupported field name format.");
      }
      results.add(deviceService.getDeviceDataField(deviceId, field));
    }

    return results;
  }

  @GET
  @Path("count/by-hardware-id")
  public int countByHardwareId(@QueryParam("hardwareIds") String hardwareIds) {
    if (StringUtils.isBlank(hardwareIds)) {
      return 0;
    } else {
      return deviceService.countByHardwareIds(
          Arrays.asList(hardwareIds.split(",")));
    }
  }

  @GET
  @Path("count/by-tags")
  public int countByTags(@QueryParam("tags") String tags) {
    return deviceService.countByTags(Arrays.asList(tags.split(",")));
  }

  @GET
  @Path("/by-partial-hardware-id/{hardwareId}")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not search for device.")
//  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public List<Device> findByPartialHardwareId(
      @PathParam("hardwareId") String hardwareId) {
    return deviceService.findByPartialHardwareId(hardwareId);
  }

}
