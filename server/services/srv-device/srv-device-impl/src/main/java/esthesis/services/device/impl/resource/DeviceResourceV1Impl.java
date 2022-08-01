package esthesis.services.device.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.common.validation.CVException;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceKey;
import esthesis.service.device.dto.DevicePage;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.service.device.resource.DeviceResourceV1;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class DeviceResourceV1Impl implements DeviceResourceV1 {

  @Inject
  DeviceService deviceService;

  @Inject
  JsonWebToken jwt;

  @Override
  public Response preregister(@Valid DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
    try {
      deviceService.preregister(deviceRegistration);
    } catch (QAlreadyExistsException e) {
      new CVException<DeviceRegistration>()
          .addViolation("ids", "One or more IDs are already registered.")
          .throwCVE();
    }

    return Response.ok().build();
  }

  @Override
  public Page<Device> find(@BeanParam Pageable pageable) {
    return deviceService.find(pageable);
  }

  @Override
  public Device get(@PathParam("id") ObjectId id) {
    return deviceService.findById(id, true);
  }

  @Override
  public void delete(@PathParam("id") ObjectId id) {
    deviceService.deleteById(id);
  }

  @Override
  public Device save(@Valid Device object) {
    return deviceService.save(object);
  }

  @Override
  @SuppressWarnings("java:S1192")
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

  @Override
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
  @Override
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//      logMessage = "Could not fetch device page field.")
//  @ReplyFilter("-shown,-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DevicePage> getDeviceDataFields(
      @PathParam("deviceId") long deviceId,
      @QueryParam("fields") String fields) {
    List<DevicePage> results = new ArrayList<>();
    for (String field : fields.split(",")) {
      if (field.split("\\.").length < 3) {
        throw new esthesis.common.exception.QMismatchException(
            "Unsupported field name format.");
      }
      results.add(deviceService.getDeviceDataField(deviceId, field));
    }

    return results;
  }

  @Override
  public int countByHardwareId(@QueryParam("hardwareIds") String hardwareIds) {
    if (StringUtils.isBlank(hardwareIds)) {
      return 0;
    } else {
      return deviceService.countByHardwareIds(
          Arrays.asList(hardwareIds.split(",")));
    }
  }

  @Override
  public int countByTags(@QueryParam("tags") String tags) {
    return deviceService.countByTags(Arrays.asList(tags.split(",")));
  }

  @Override
  public List<Device> findByPartialHardwareId(
      @PathParam("hardwareId") String hardwareId) {
    return deviceService.findByPartialHardwareId(hardwareId);
  }

}
