package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.common.device.ProvisioningInfoRequest;
import esthesis.common.device.ProvisioningInfoResponse;
import esthesis.common.device.ProvisioningRequest;
import esthesis.common.device.RegistrationRequest;
import esthesis.common.device.RegistrationResponse;
import esthesis.common.dto.DeviceMessage;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.config.AppSettings.SettingValues.Provisioning.Encryption;
import esthesis.platform.server.service.CAService;
import esthesis.platform.server.service.DeviceAgentService;
import esthesis.platform.server.service.ProvisioningService;
import esthesis.platform.server.service.SettingResolverService;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import lombok.extern.java.Log;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Log
@Validated
@RestController
@RequestMapping("/agent")
public class DeviceAgentResource {

  private final SettingResolverService srs;
  private final ProvisioningService provisioningService;
  private final DeviceAgentService deviceAgentService;

  public DeviceAgentResource(SettingResolverService srs, ProvisioningService provisioningService,
    CAService caService, DeviceAgentService deviceAgentService) {
    this.srs = srs;
    this.provisioningService = provisioningService;
    this.deviceAgentService = deviceAgentService;
  }

  @PostMapping(path = "/register")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device.")
  public DeviceMessage<RegistrationResponse> register(
    @Valid @RequestBody DeviceMessage<RegistrationRequest> registrationRequest)
  throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException,
         InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException,
         OperatorCreationException, NoSuchProviderException {
    return deviceAgentService.register(registrationRequest);
  }

  @PostMapping({"/provisioning/info/{id}", "/provisioning/info"})
  public ResponseEntity<DeviceMessage<ProvisioningInfoResponse>> provisioningInfo(
    @PathVariable Optional<Long> id,
    @Valid @RequestBody DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
         SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    return ResponseEntity.ok()
      .body(deviceAgentService.provisioningInfo(id, provisioningInfoRequest));
  }

  @PostMapping(value = "/provisioning/download/{id}")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not download "
    + "provisioning package.")
  public ResponseEntity provisioningDownload(@PathVariable long id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
         SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + provisioningService.findById(id).getFileName() + (
          srs.is(Provisioning.ENCRYPTION, Encryption.ENCRYPTED) ? ".encrypted" : ""
        ))
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(deviceAgentService.provisioningDownload(id, provisioningRequest));
  }
}
