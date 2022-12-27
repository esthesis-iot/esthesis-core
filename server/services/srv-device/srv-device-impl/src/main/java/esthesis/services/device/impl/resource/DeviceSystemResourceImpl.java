package esthesis.services.device.impl.resource;

import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.device.impl.service.DeviceRegistrationService;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
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
  public String findPublicKey(String hardwareId) {
    return deviceService.findByHardwareId(hardwareId, false).orElseThrow()
        .getDeviceKey().getPublicKey();
  }
}
