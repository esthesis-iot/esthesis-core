package esthesis.services.device.impl.resource;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.device.dto.DeviceWithAttributesDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.device.impl.service.DeviceRegistrationService;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.bouncycastle.operator.OperatorCreationException;

public class DeviceSystemResourceImpl implements DeviceSystemResource {

  @Inject
  DeviceRegistrationService deviceRegistrationService;

  @Inject
  DeviceService deviceService;

  @Override
  public DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
         OperatorCreationException, NoSuchProviderException {
    return deviceRegistrationService.register(deviceRegistration);
  }

  @Override
  public DeviceEntity findByHardwareId(String hardwareId) {
    return deviceService.findByHardwareId(hardwareId, false).orElseThrow();
  }

  @Override
  public DeviceEntity findById(String esthesisId) {
    return deviceService.findById(esthesisId);
  }

  @Override
  public String findPublicKey(String hardwareId) {
    return deviceService.findByHardwareId(hardwareId, false).orElseThrow()
        .getDeviceKey().getPublicKey();
  }

  /**
   * Returns the list of attributes for a device.
   *
   * @param hardwareId The hardware ID of the device.
   */
  @Override
  public List<DeviceAttributeEntity> getDeviceAttributesByEsthesisId(String esthesisId) {
    DeviceEntity deviceEntity = deviceService.findById(esthesisId);
    if (deviceEntity != null) {
      return deviceService.getProfile(deviceEntity.getId().toHexString()).getAttributes();
    } else {
      throw new QDoesNotExistException("Device with esthesis ID '{}'does not exist.", esthesisId);
    }
  }

  @Override
  public List<String> getDeviceIds() {
    return deviceService.getDevicesIds();
  }

}
