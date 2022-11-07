package esthesis.services.device.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVException;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceKey;
import esthesis.service.device.dto.DeviceProfileNote;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.device.impl.service.DeviceProfileService;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
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

public class DeviceResourceImpl implements DeviceResource {

  @Inject
  DeviceService deviceService;

  @Inject
  DeviceProfileService deviceProfileService;

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
  public Device activatePreregisteredDevice(String hardwareId) {
    return deviceService.activatePreregisteredDevice(hardwareId);
  }

  @Override
  public Page<Device> find(@BeanParam Pageable pageable) {
    return deviceService.find(pageable, true);
  }

  @GET
  @Override
  @Path("/v1/device/{id}")
  @JSONReplyFilter(filter = "hardwareId,id,status,tags,lastSeen")
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
    String stringBuilder =
        "****************************************************************\n"
            + "PUBLIC KEY\n"
            + "****************************************************************\n"
            + deviceKeys.getPublicKey()
            + "\n"
            + "****************************************************************\n"
            + "PRIVATE KEY\n"
            + "****************************************************************\n"
            + deviceKeys.getPrivateKey()
            + "\n";
    // TODO we don't need the certificate too?

    return Response
        .ok(stringBuilder)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + filename)
        .type(MediaType.APPLICATION_OCTET_STREAM)
        .build();
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

  @Override
  public List<DeviceProfileNote> getDeviceProfileNotes(String deviceId) {
    return deviceProfileService.getProfile(deviceId);
  }

  @Override
  public List<DeviceProfileNote> saveDeviceProfileNotes(
      Map<String, String> fields,
      String deviceId) {
    return deviceProfileService.saveProfile(deviceId, fields);
  }

  @Override
  public DeviceProfileNote addDeviceProfileNote(
      DeviceProfileNote field) {
    return deviceProfileService.createProfileField(field);
  }

  @Override
  public void deleteDeviceProfileNote(String deviceId, String keyName) {
    deviceProfileService.deleteProfileField(deviceId, keyName);
  }

}
