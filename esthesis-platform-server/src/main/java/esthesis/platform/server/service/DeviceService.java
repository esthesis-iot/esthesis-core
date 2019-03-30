package esthesis.platform.server.service;


import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.platform.common.request.RegistrationRequest;
import esthesis.platform.common.response.RegistrationResponse;
import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.config.AppConstants.Device.Status;
import esthesis.platform.common.config.AppConstants.Generic;
import esthesis.platform.server.config.AppConstants.Setting;
import esthesis.platform.server.config.AppConstants.WebSocket.Topic;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.mapper.DeviceMapper;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.util.RegistrationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
  private final SettingsService settingsService;

  public DeviceService(RegistrationUtil registrationUtil,
      DeviceRepository deviceRepository, DeviceMapper deviceMapper,
      WebSocketService webSocketService, SettingsService settingsService) {
    this.registrationUtil = registrationUtil;
    this.deviceRepository = deviceRepository;
    this.deviceMapper = deviceMapper;
    this.webSocketService = webSocketService;
    this.settingsService = settingsService;
  }

  /**
   * Preregister a device, so that it can self-register later on.
   *
   * @param ids The IDs of the devices to preregister.
   */
  public void preregister(String[] ids) {
    //TODO check for existing device IDs.
    List<DeviceDTO> devices = new ArrayList<>();
    for (String id : ids) {
      devices.add(new DeviceDTO().setDeviceId(id).setState(Status.PREREGISTERED));
    }

    deviceRepository.saveAll(deviceMapper.mapDTOs(devices));
  }

  public void register(String deviceId, String hmac, String tags)
      throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeyException {
    // Check registration preconditions and register device.
    DeviceDTO deviceDTO = null;
    switch (settingsService.getSetting(Generic.SYSTEM, Setting.DEVICE_REGISTRATION, Generic.SYSTEM).getVal()) {
      case AppConstants.Device.RegistrationMode.OPEN:
        registrationUtil.checkRegistrationEnabled();
        registrationUtil.checkDeviceIdDoesNotExist(deviceId);
        registrationUtil.checkTagsExist(tags);
        deviceDTO = deviceMapper.map(registrationUtil.registerNew(deviceId, tags));
        break;
      case AppConstants.Device.RegistrationMode.OPEN_WITH_APPROVAL:
        registrationUtil.checkRegistrationEnabled();
        registrationUtil.checkDeviceIdDoesNotExist(deviceId);
        registrationUtil.checkTagsExist(tags);
        deviceDTO = deviceMapper.map(registrationUtil.registerForApproval(deviceId, tags));
        break;
      case AppConstants.Device.RegistrationMode.ID:
        registrationUtil.checkRegistrationEnabled();
        registrationUtil.checkDeviceIdPreregistered(deviceId);
        registrationUtil.checkTagsExist(tags);
        deviceDTO = deviceMapper.map(registrationUtil.registerPreregistered(deviceId, tags));
        break;
      case AppConstants.Device.RegistrationMode.CRYPTO:
        registrationUtil.checkRegistrationEnabled();
        registrationUtil.checkDeviceIdPreregistered(deviceId);
        registrationUtil.checkTagsExist(tags);
        registrationUtil.checkHmac(deviceId, hmac);
        deviceDTO = deviceMapper.map(registrationUtil.registerCrypto(deviceId, tags));
        break;
      case AppConstants.Device.RegistrationMode.DISABLED:
        throw new QDisabledException("Device registration is disabled");
      default:
        throw new QDoesNotExistException("Platform server runs on an an unknown registration mode.");
    }

    // Realtime notification.
    webSocketService.publish(new WebSocketMessageDTO()
        .setTopic(Topic.DEVICE_REGISTRATION)
        .setPayload(MessageFormat.format("Device with id {0} registered.", deviceId)));
  }

  public RegistrationResponse register(RegistrationRequest registrationRequest)
      throws NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeyException {
    register(registrationRequest.getDeviceId(), registrationRequest.getHmac(), registrationRequest.getTags());
    final Device device = ReturnOptional.r(deviceRepository.findByDeviceId(registrationRequest.getDeviceId()));

    return new RegistrationResponse()
        .setPrivateKey(device.getPrivateKey())
        .setPublicKey(device.getPublicKey());
  }
}
