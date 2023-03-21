package esthesis.services.device.impl.resource;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.common.validation.CVExceptionContainer;
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

  /**
   * Preregister devices in the platform. DeviceRegistrationDTO.hardwareId may content multiple
   * devices in this case, separated by new lines.
   */
  @Override
  @Audited(cat = Category.DEVICE, op = Operation.UPDATE, msg = "Preregistering device")
  public Response preregister(@Valid DeviceRegistrationDTO deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
    try {
      deviceRegistrationService.preregister(deviceRegistration);
    } catch (QAlreadyExistsException e) {
      new CVExceptionContainer<DeviceRegistrationDTO>()
          .addViolation("ids", "One or more IDs are already registered.")
          .throwCVE();
    }

    return Response.ok().build();
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.UPDATE, msg = "Activating preregistered device")
  public DeviceEntity activatePreregisteredDevice(String hardwareId) {
    return deviceRegistrationService.activatePreregisteredDevice(hardwareId);
  }
}
