package esthesis.services.device.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.device.impl.service.DeviceService;
import esthesis.services.device.impl.service.DeviceTagService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

public class DeviceResourceImpl implements DeviceResource {

	@Inject
	DeviceService deviceService;

	@Inject
	DeviceTagService deviceTagService;

	@Override
	@Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Search devices", log =
		AuditLogType.DATA_IN)
	public Page<DeviceEntity> find(@BeanParam Pageable pageable) {
		return deviceService.find(pageable, true);
	}

	@GET
	@Override
	@Path("/v1/{id}")
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
	@Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Download device", log =
		AuditLogType.DATA_IN)
	public Response download(String deviceId, AppConstants.KeyType type) {
		DeviceEntity deviceEntity = deviceService.findById(deviceId);

		String content;
		String filename = Slugify.builder().underscoreSeparator(true).build()
			.slugify(deviceEntity.getHardwareId());
		switch (type) {
			case PRIVATE -> {
				filename += ".key";
				content = deviceService.getPrivateKey(deviceId);
			}
			case PUBLIC -> {
				filename += ".pub";
				content = deviceService.getPublicKey(deviceId);
			}
			case CERTIFICATE -> {
				filename += ".crt";
				content = deviceService.getCertificate(deviceId);
			}
			default -> throw new QDoesNotExistException("Key type '{}' is not valid.", type);
		}
		return ResponseBuilder.ok(content)
			.header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
	}


	@Override
	public DeviceProfileDTO getProfile(String deviceId) {
		return deviceService.getProfile(deviceId);
	}

	@Override
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save a device profile")
	public void saveProfile(String deviceId, DeviceProfileDTO deviceProfileDTO) {
		deviceService.saveProfile(deviceId, deviceProfileDTO);
	}

//  @Override
//  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Create a device attribute")
//  public DeviceAttributeEntity addDeviceAttribute(
//      String deviceId, DeviceAttributeEntity deviceAttributeEntity) {
//    return deviceService.createAttribute(deviceId, deviceAttributeEntity);
//  }

//  @Override
//  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Delete a device attribute")
//  public void deleteDeviceAttribute(String deviceId, String keyName) {
//    deviceService.deleteAttribute(deviceId, keyName);
//  }

//  @Override
//  public List<DeviceProfileFieldDataDTO> getDeviceFields(String deviceId) {
//    return deviceService.getProfileFields(deviceId);
//  }

	@Override
	public List<DeviceProfileFieldDataDTO> getDeviceData(String deviceId) {
		return deviceService.getDeviceData(deviceId);
	}
}
