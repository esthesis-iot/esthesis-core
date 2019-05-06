package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingSignature;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DevicesResource.class.getName());

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final CertificatesService certificatesService;
  private final SecurityService securityService;

  public DevicesResource(DeviceService deviceService, SettingResolverService srs,
    CertificatesService certificatesService,
    SecurityService securityService) {
    this.deviceService = deviceService;
    this.srs = srs;
    this.certificatesService = certificatesService;
    this.securityService = securityService;
  }

  @PostMapping(value = "/preregister")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device(s).")
  public ResponseEntity preregister(@Valid @RequestBody DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
         NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
         InvalidKeyException {
    deviceService.preregister(deviceRegistrationDTO);

    return ResponseEntity.ok().build();
  }

  @EmptyPredicateCheck
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  @ReplyPageableFilter("createdOn,hardwareId,id,state")
  public Page<DeviceDTO> findAll(@QuerydslPredicate(root = Device.class) Predicate predicate,
    Pageable pageable) {
    return deviceService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device.")
  @ReplyFilter("-certificate,-privateKey,-publicKey")
  public DeviceDTO get(@PathVariable long id) {
    return deviceService.findById(id);
  }

  @PostMapping(value = "/register")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device.")
  public DeviceMessage<RegistrationResponse> register(
    @Valid @RequestBody DeviceMessage<RegistrationRequest> msg)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, IOException,
         InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException {
    // Verify signature and decrypt according to the configuration.
    DeviceDTO deviceDTO;
    try {
      deviceDTO = deviceService.findByHardwareId(msg.getHardwareId());
      securityService.processIncomingMessage(msg, DeviceRegistration.class, deviceDTO);
    } catch (QDoesNotExistException e) {
      // If the device is not preregistered it is not possible to verify the signature nor to
      // decrypt its payload.
      if (srs.is(Security.INCOMING_SIGNATURE, IncomingSignature.SIGNED) ||
        srs.is(Security.INCOMING_ENCRYPTION, IncomingEncryption.ENCRYPTED)) {
        throw new QSecurityException("Signature and/or decryption is not supported during "
          + "registration unless the device is preregistered with the platform.");
      }
    }

    // Proceed to registration.
    deviceDTO = deviceService.register(msg.getPayload(), msg.getHardwareId());
    RegistrationResponse registrationResponse = new RegistrationResponse();
    registrationResponse.setPublicKey(deviceDTO.getPublicKey());
    registrationResponse.setPrivateKey(deviceDTO.getPrivateKey());
    registrationResponse.setSessionKey(deviceDTO.getSessionKey());
    registrationResponse.setPsPublicKey(
      certificatesService.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE)).getPublicKey());

    // Wrap the reply.
    DeviceMessage<RegistrationResponse> responseDeviceMessage = new DeviceMessage<>();
    responseDeviceMessage.setPayload(registrationResponse);

    securityService
      .prepareOutgoingMessage(msg, deviceService.findByHardwareId(msg.getHardwareId()));

    return responseDeviceMessage;
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete device.")
  public void delete(@PathVariable long id) {
    deviceService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Device.")
  public DeviceDTO save(@Valid @RequestBody DeviceDTO object) {
    return deviceService.save(object);
  }
}
