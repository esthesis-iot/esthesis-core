package esthesis.services.device.impl.resource;

import esthesis.common.exception.QAlreadyExistsException;
import esthesis.service.common.validation.CVException;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceRegistrationResource;
import esthesis.services.device.impl.service.DeviceRegistrationService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;

public class DeviceRegistrationResourceImpl implements
    DeviceRegistrationResource {

  @Inject
  DeviceRegistrationService deviceRegistrationService;

  @Override
  public Response preregister(@Valid DeviceRegistrationDTO deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
    try {
      deviceRegistrationService.preregister(deviceRegistration);
    } catch (QAlreadyExistsException e) {
      new CVException<DeviceRegistrationDTO>()
          .addViolation("ids", "One or more IDs are already registered.")
          .throwCVE();
    }

    return Response.ok().build();
  }

  @Override
  public DeviceEntity activatePreregisteredDevice(String hardwareId) {
    return deviceRegistrationService.activatePreregisteredDevice(hardwareId);
  }
}
