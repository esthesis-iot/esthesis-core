package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.platform.backend.common.device.ProvisioningInfoRequest;
import esthesis.platform.backend.common.device.ProvisioningInfoResponse;
import esthesis.platform.backend.common.device.ProvisioningRequest;
import esthesis.platform.backend.common.device.RegistrationRequest;
import esthesis.platform.backend.common.device.RegistrationResponse;
import esthesis.platform.backend.common.dto.DeviceMessage;
import esthesis.platform.backend.server.service.DeviceAgentService;
import esthesis.platform.backend.server.service.ProvisioningService;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
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

@Log
@Validated
@RestController
@RequestMapping("/agent")
public class DeviceAgentResource {

  private final ProvisioningService provisioningService;
  private final DeviceAgentService deviceAgentService;

  public DeviceAgentResource(ProvisioningService provisioningService,
    DeviceAgentService deviceAgentService) {
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
    @Valid @RequestBody DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest) {
    return ResponseEntity.ok()
      .body(deviceAgentService.provisioningInfo(id, provisioningInfoRequest));
  }

  @PostMapping(value = "/provisioning/download/{id}")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not download "
    + "provisioning package.")
  public ResponseEntity provisioningDownload(@PathVariable long id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest)
  throws IOException {
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + provisioningService.findById(id).getFileName())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(deviceAgentService.provisioningDownload(id, provisioningRequest));
  }
}
