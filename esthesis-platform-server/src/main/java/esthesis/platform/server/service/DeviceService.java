package esthesis.platform.server.service;


import static esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode.DISABLED;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.platform.server.config.AppConstants.Device.Status;
import esthesis.platform.server.config.AppConstants.WebSocket.Topic;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.mapper.DeviceMapper;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.util.RegistrationUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DeviceService extends BaseService<DeviceDTO, Device> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DeviceService.class.getName());

  private final RegistrationUtil registrationUtil;
  private final DeviceRepository deviceRepository;
  private final DeviceMapper deviceMapper;
  private final WebSocketService webSocketService;
  private final SettingResolverService srs;
  private final TagService tagService;

  public DeviceService(RegistrationUtil registrationUtil,
    DeviceRepository deviceRepository, DeviceMapper deviceMapper,
    WebSocketService webSocketService, SettingResolverService srs,
    TagService tagService) {
    this.registrationUtil = registrationUtil;
    this.deviceRepository = deviceRepository;
    this.deviceMapper = deviceMapper;
    this.webSocketService = webSocketService;
    this.srs = srs;
    this.tagService = tagService;
  }

  /**
   * Preregister a device, so that it can self-register later on.
   */
  @Async
  public void preregister(DeviceRegistrationDTO deviceRegistrationDTO)
    throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
    // Split IDs.
    String ids = deviceRegistrationDTO.getIds();
    ids = ids.replace("\n", ",");
    String[] idList = ids.split(",");

    // Before preregistering the devices check that all given registration IDs are available. If any
    // of the given IDs is already assigned on an existing device abort the preregistration.
    for (String hardwareId : idList) {
      if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
        throw new QAlreadyExistsException("Preregistration ID {0} is already assigned to a device "
          + "registered in the system.", hardwareId);
      }
    }
    // Convert tags to their name-equivalent.
    String tagNames = String.join(",",deviceRegistrationDTO.getTags().stream().map(tagId -> {
      return tagService.findById(tagId).getName();
    }).collect(Collectors.toList()));

    // Register IDs.
    for (String hardwareId : idList) {
      registrationUtil.register(hardwareId, tagNames, Status.PREREGISTERED, false);
    }
  }

  public DeviceDTO register(RegistrationRequest registrationRequest)
    throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeyException {
    DeviceDTO deviceDTO = null;

    if (srs.is(DeviceRegistration.REGISTRATION_MODE, DISABLED)) {
      throw new QDisabledException(
        "Attempting to register device with hardware ID {0} but registration of new devices is "
          + "disabled.",
        registrationRequest.getHardwareId());
    } else {
      LOGGER.log(Level.FINE, "Attempting to register device with registration ID {0}.",
        registrationRequest.getHardwareId());
      // Check registration preconditions and register device.
      LOGGER.log(Level.FINEST, "Platform running on {0} registration mode.",
        srs.get(DeviceRegistration.REGISTRATION_MODE));
      switch (srs.get(DeviceRegistration.REGISTRATION_MODE)) {
        case RegistrationMode.OPEN:
          deviceDTO = deviceMapper
            .map(registrationUtil.register(registrationRequest.getHardwareId(),
              registrationRequest.getTags(), Status.REGISTERED, true));
          break;
        case RegistrationMode.OPEN_WITH_APPROVAL:
          deviceDTO = deviceMapper
            .map(registrationUtil.register(registrationRequest.getHardwareId(),
              registrationRequest.getTags(), Status.APPROVAL, true));
          break;
        case RegistrationMode.ID:
          deviceDTO = deviceMapper.map(registrationUtil.registerPreregistered(registrationRequest));
          break;
        case RegistrationMode.DISABLED:
          throw new QDisabledException("Device registration is disabled.");
      }

      // Realtime notification.
      webSocketService.publish(new WebSocketMessageDTO()
        .setTopic(Topic.DEVICE_REGISTRATION)
        .setPayload(MessageFormat
          .format("Device with registration id {0} registered.",
            registrationRequest.getHardwareId())));
    }

    LOGGER.log(Level.FINE, "Registered device with hardware ID {0}.",
      registrationRequest.getHardwareId());
    return deviceDTO;
  }

  public DeviceDTO findByHardwareId(String hardwareId) {
    return deviceMapper
      .map(ReturnOptional.r(deviceRepository.findByHardwareId(hardwareId), hardwareId));
  }

  @Async
  @Override
  public DeviceDTO deleteById(long id) {
    return super.deleteById(id);
  }
}
