package esthesis.services.device.impl.resource;

import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.inject.Inject;
import org.bouncycastle.operator.OperatorCreationException;

public class DeviceSystemResourceImpl implements DeviceSystemResource {

  @Inject
  DeviceService deviceService;

  @Override
  public Device register(DeviceRegistration deviceRegistration)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
         OperatorCreationException, NoSuchProviderException {
    return deviceService.register(deviceRegistration);
  }
}
