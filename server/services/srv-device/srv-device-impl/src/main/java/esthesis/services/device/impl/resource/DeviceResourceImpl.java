package esthesis.services.device.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.device.impl.service.DeviceService;
import esthesis.services.device.impl.service.DeviceTagService;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

public class DeviceResourceImpl implements DeviceResource {

  @Inject
  DeviceService deviceService;

  @Inject
  DeviceTagService deviceTagService;

  @Inject
  JsonWebToken jwt;

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Search devices", log =
      AuditLogType.DATA_IN)
  public Page<DeviceEntity> find(@BeanParam Pageable pageable) {
    return deviceService.find(pageable, true);
  }

  @GET
  @Override
  @Path("/v1/{id}")
  @JSONReplyFilter(filter = "hardwareId,id,status,tags,lastSeen,registeredOn")
  @Audited(cat = Category.DEVICE, op = Operation.READ, msg = "View device")
  public DeviceEntity get(@PathParam("id") String id) {
    return deviceService.findById(id);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.DELETE, msg = "Delete device")
  public void delete(@PathParam("id") String id) {
    deviceService.deleteById(id);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save device")
  public DeviceEntity save(@Valid DeviceEntity object) {
    return deviceService.save(object);
  }

  @Override
  public Long countByHardwareIds(
      @QueryParam("hardwareIds") String hardwareIds,
      @QueryParam("partialMatch") boolean partialMatch) {
    if (StringUtils.isBlank(hardwareIds)) {
      return 0L;
    } else {
      return deviceService.countByHardwareId(
          Arrays.asList(hardwareIds.split(",")), partialMatch);
    }
  }

  @Override
  public List<DeviceEntity> findByHardwareIds(String hardwareIds, boolean partialMatch) {
    return deviceService.findByHardwareId(Arrays.asList(hardwareIds.split(",")), partialMatch);
  }

  @Override
  public List<DeviceEntity> findByTagName(String tag) {
    return deviceTagService.findByTagName(tag, false);
  }

  @Override
  public List<DeviceEntity> findByTagId(String tagId) {
    return deviceTagService.findByTagId(tagId);
  }

  @Override
  public Long countByTags(@QueryParam("tags") String tags,
      @QueryParam("partialMatch") boolean partialMatch) {
    return deviceTagService.countByTag(Arrays.asList(tags.split(",")),
        partialMatch);
  }

  @Override
  public GeolocationDTO getDeviceGeolocation(String deviceId) {
    return deviceService.getGeolocation(deviceId);
  }

  @Override
  @SuppressWarnings("java:S1192")
  @Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Download device public key")
  public Response downloadPublicKey(String id) {
    return ResponseBuilder.ok(deviceService.downloadPublicKey(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-public-key.pem").build()
        .toResponse();
  }

  @Override
  @SuppressWarnings("java:S1192")
  @Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Download device private key")
  public Response downloadPrivateKey(String id) {
    return ResponseBuilder.ok(deviceService.downloadPrivateKey(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-private-key.pem").build()
        .toResponse();
  }

  @Override
  @SuppressWarnings("java:S1192")
  @Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Download device certificate")
  public Response downloadCertificate(String id) {
    return ResponseBuilder.ok(deviceService.downloadCertificate(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-certificate.pem").build()
        .toResponse();
  }

}
