package esthesis.services.device.impl.resource;

import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.dto.GeolocationDTO;
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
import org.bson.types.ObjectId;
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
  public Page<DeviceEntity> find(@BeanParam Pageable pageable) {
    return deviceService.find(pageable, true);
  }

  @GET
  @Override
  @Path("/v1/device/{id}")
  @JSONReplyFilter(filter = "hardwareId,id,status,tags,lastSeen")
  public DeviceEntity get(@PathParam("id") ObjectId id) {
    return deviceService.findById(id);
  }

  @Override
  public void delete(@PathParam("id") ObjectId id) {
    deviceService.deleteById(id);
  }

  @Override
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
  public List<DeviceEntity> findByHardwareIds(String hardwareIds,
      boolean partialMatch) {
    return deviceService.findByHardwareId(
        Arrays.asList(hardwareIds.split(",")), partialMatch);
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
  public Response downloadPublicKey(ObjectId id) {
    return ResponseBuilder.ok(deviceService.downloadPublicKey(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-public-key.pem").build()
        .toResponse();
  }

  @Override
  public Response downloadPrivateKey(ObjectId id) {
    return ResponseBuilder.ok(deviceService.downloadPrivateKey(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-private-key.pem").build()
        .toResponse();
  }

  @Override
  public Response downloadCertificate(ObjectId id) {
    return ResponseBuilder.ok(deviceService.downloadCertificate(id))
        .header("Content-Disposition",
            "attachment; filename=" + id + "-certificate.pem").build()
        .toResponse();
  }

}
