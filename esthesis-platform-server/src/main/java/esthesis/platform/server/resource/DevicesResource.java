package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.core.types.Predicate;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.IgnoreDuringDeviceRegistration;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingSignature;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingSignature;
import esthesis.platform.server.dto.CertificateDTO;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final CertificatesService certificatesService;
  private final SecurityService securityService;

  public DevicesResource(DeviceService deviceService, SettingResolverService srs,
      CertificatesService certificatesService, SecurityService securityService) {
    this.deviceService = deviceService;
    this.srs = srs;
    this.certificatesService = certificatesService;
    this.securityService = securityService;
  }

  private void decrypt(DeviceMessage deviceMessage)
      throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
    final String psPrivateKey = certificatesService.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE))
        .getPrivateKey();
    deviceMessage.decrypt(psPrivateKey, RegistrationRequest.class);
  }

  private void verify(DeviceMessage deviceMessage)
      throws InvalidKeySpecException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    DeviceDTO deviceDTO;
    String hardwareId = ((RegistrationRequest) deviceMessage.getPayload()).getHardwareId();
    try {
      deviceDTO = deviceService
          .findByHardwareId(hardwareId);
    } catch (QDoesNotExistException e) {
      throw new QSecurityException("Could not find device with registration ID {0} to verify the signature.",
        hardwareId);
    }
    deviceMessage.verify(deviceDTO.getPublicKey());
  }

  private void sign(DeviceMessage deviceMessage)
      throws InvalidKeySpecException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    final CertificateDTO certificateDTO = certificatesService.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE));
    deviceMessage.sign(certificateDTO.getPrivateKey());
  }

  @PostMapping(value = "/preregister")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device(s).")
  public ResponseEntity preregister(@Valid @RequestBody DeviceRegistrationDTO deviceRegistrationDTO)
    throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
    deviceService.preregister(deviceRegistrationDTO);

    return ResponseEntity.ok().build();
  }

  @EmptyPredicateCheck
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  @ReplyPageableFilter("createdOn,hardwareId,id,state")
  public Page<DeviceDTO> findAll(@QuerydslPredicate(root = Device.class) Predicate predicate, Pageable pageable) {
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
  public DeviceMessage<RegistrationResponse> registerDevice(
      @Valid @RequestBody DeviceMessage<RegistrationRequest> requestDeviceMessage)
      throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException,
      BadPaddingException, InvalidKeyException, IOException, SignatureException, NoSuchProviderException {
    // Decrypt the message (if required so).
    switch (srs.get(Security.INCOMING_ENCRYPTION)) {
      case IncomingEncryption.ENCRYPTED:
        if (requestDeviceMessage.getEncryptedPayload() != null) {
          decrypt(requestDeviceMessage);
        } else {
          if (srs.isNotAny(DeviceRegistration.IGNORE_DURING_DEVICE_REGISTRATION,
              IgnoreDuringDeviceRegistration.ENCRYPTION_AND_SIGNATURE,
              IgnoreDuringDeviceRegistration.ENCRYPTION)) {
            throw new QSecurityException(
                "Discarding non-encrypted message as encryption of incoming device messages is set to required.");
          }
        }
        break;
      case IncomingEncryption.NOT_ENCRYPTED:
        if (requestDeviceMessage.getEncryptedPayload() != null) {
          if (srs.isNotAny(DeviceRegistration.IGNORE_DURING_DEVICE_REGISTRATION,
              IgnoreDuringDeviceRegistration.ENCRYPTION_AND_SIGNATURE,
              IgnoreDuringDeviceRegistration.ENCRYPTION)) {
            throw new QSecurityException(
                "Discarding encrypted message as encryption of incoming messages is set to not-supported.");
          } else {
            decrypt(requestDeviceMessage);
          }
        }
        break;
      case IncomingEncryption.OPTIONAL:
        if (requestDeviceMessage.getEncryptedPayload() != null) {
          decrypt(requestDeviceMessage);
        }
        break;
    }

    // Verify the message (if required so).
    switch (srs.get(Security.INCOMING_SIGNATURE)) {
      case IncomingSignature.SIGNED:
        if (requestDeviceMessage.getSignature() != null) {
          verify(requestDeviceMessage);
        } else {
          if (srs.isNotAny(DeviceRegistration.IGNORE_DURING_DEVICE_REGISTRATION,
              IgnoreDuringDeviceRegistration.ENCRYPTION_AND_SIGNATURE,
              IgnoreDuringDeviceRegistration.SIGNATURE)) {
            throw new QSecurityException("Discarding message without signature.");
          }
        }
        break;
      case IncomingSignature.NOT_SIGNED:
        if (requestDeviceMessage.getSignature() != null) {
          if (srs.isNotAny(DeviceRegistration.IGNORE_DURING_DEVICE_REGISTRATION,
              IgnoreDuringDeviceRegistration.ENCRYPTION_AND_SIGNATURE,
              IgnoreDuringDeviceRegistration.SIGNATURE)) {
            throw new QSecurityException(
                "Discarding message with signature as the platform operates on no-signature mode.");
          } else {
            verify(requestDeviceMessage);
          }
        }
        break;
      case IncomingSignature.OPTIONAL:
        if (requestDeviceMessage.getSignature() != null) {
          verify(requestDeviceMessage);
        }
        break;
    }

    // Proceed to registration.
    RegistrationResponse registrationResponse = new RegistrationResponse();
    final DeviceDTO deviceDTO = deviceService.register(requestDeviceMessage.getPayload());
    registrationResponse.setPublicKey(deviceDTO.getPublicKey());
    registrationResponse.setPrivateKey(securityService.decrypt(deviceDTO.getPrivateKey()));
    registrationResponse
        .setPsPublicKey(certificatesService.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE)).getPublicKey());

    // Wrap the reply.
    DeviceMessage<RegistrationResponse> responseDeviceMessage = new DeviceMessage<>();
    responseDeviceMessage.setPayload(registrationResponse);
    switch (srs.get(Security.OUTGOING_SIGNATURE)) {
      case OutgoingSignature.SIGNED:
        sign(responseDeviceMessage);
        break;
      case OutgoingSignature.DEVICE_SPECIFIC:
        if (requestDeviceMessage.getPayload().isSupportsIncomingSignatureVerification()) {
          sign(responseDeviceMessage);
        }
        break;
    }

    return responseDeviceMessage;
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete device.")
  public void delete(@PathVariable long id) {
    deviceService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Device.")
  public DeviceDTO save(@Valid @RequestBody DeviceDTO object) {
    return deviceService.save(object);
  }
}
