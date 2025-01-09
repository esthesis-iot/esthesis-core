package esthesis.services.device.impl.resource;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.AppConstants;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.device.impl.service.DeviceRegistrationService;
import esthesis.services.device.impl.service.DeviceService;
import esthesis.services.device.impl.service.DeviceTagService;
import jakarta.annotation.security.RolesAllowed;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;

@RequiredArgsConstructor
public class DeviceSystemResourceImpl implements DeviceSystemResource {

	private final DeviceRegistrationService deviceRegistrationService;
	private final DeviceService deviceService;
	private final DeviceTagService deviceTagService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
				 OperatorCreationException, NoSuchProviderException {
		return deviceRegistrationService.register(deviceRegistration);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public DeviceEntity findByHardwareId(String hardwareId) {
		return deviceService.findByHardwareId(hardwareId, false).orElseThrow();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public DeviceEntity findById(String esthesisId) {
		return deviceService.findById(esthesisId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public String findPublicKey(String hardwareId) {
		return deviceService.findByHardwareId(hardwareId, false).orElseThrow()
			.getDeviceKey().getPublicKey();
	}

	/**
	 * Returns the list of attributes for a device.
	 *
	 * @param esthesisId The esthesis ID of the device.
	 */
	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public List<DeviceAttributeEntity> getDeviceAttributesByEsthesisId(String esthesisId) {
		DeviceEntity deviceEntity = deviceService.findById(esthesisId);
		if (deviceEntity != null) {
			return deviceService.getProfile(deviceEntity.getId().toHexString()).getAttributes();
		} else {
			throw new QDoesNotExistException("Device with esthesis ID '{}'does not exist.", esthesisId);
		}
	}

	/**
	 * Returns the list of attributes for a device.
	 *
	 * @param esthesisHardwareId The esthesis hardware ID of the device.
	 * @return
	 */
	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public List<DeviceAttributeEntity> getDeviceAttributesByEsthesisHardwareId(
		String esthesisHardwareId) {
		Optional<DeviceEntity> byHardwareId = deviceService.findByHardwareId(esthesisHardwareId, false);
		if (byHardwareId.isPresent()) {
			return deviceService.getProfile(byHardwareId.get().getId().toHexString()).getAttributes();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public List<String> getDeviceIds() {
		return deviceService.getDevicesIds();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public Optional<DeviceAttributeEntity> getDeviceAttributeByEsthesisHardwareIdAndAttributeName(
		String esthesisHardwareId, String attributeName) {
		return deviceService.getDeviceAttributeByName(
			deviceService.findByHardwareId(esthesisHardwareId, false).orElseThrow().getId().toHexString(),
			attributeName);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public DevicesLastSeenStatsDTO getDeviceStats() {
		return deviceService.getDeviceStats();
	}

	@Override
	public List<DeviceEntity> getLatestDevices(int limit) {
		return deviceService.getLatestDevices(limit);
	}

	/**
	 * Returns the hardware IDs of devices that have the specified tags.
	 *
	 * @param tags a comma-separated list of tags.
	 * @return a list of hardware IDs.
	 */
	@Override
	public List<String> findByTagNames(String tags) {
		if (StringUtils.isNotEmpty(tags)) {
			return deviceTagService.findByTagName(List.of(tags.split(",")), false).stream()
				.map(DeviceEntity::getHardwareId).toList();
		} else {
			return List.of();
		}
	}

}
