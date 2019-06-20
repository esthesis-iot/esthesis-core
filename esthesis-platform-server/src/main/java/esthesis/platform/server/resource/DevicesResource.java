package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.extension.common.util.Base64E;
import esthesis.extension.datasink.dto.FieldDTO;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceKeyDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.service.DevicePageService;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.SecurityService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  private final DeviceService deviceService;
  private final SecurityService securityService;
  private final DevicePageService devicePageService;

  public DevicesResource(DeviceService deviceService, SecurityService securityService,
    DevicePageService devicePageService) {
    this.deviceService = deviceService;
    this.securityService = securityService;
    this.devicePageService = devicePageService;
  }

  @PostMapping(path = "/preregister")
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
  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public DeviceDTO get(@PathVariable long id) {
    final DeviceDTO deviceDTO = deviceService.findById(id, true);

    return deviceDTO;
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

  @GetMapping(path = "{deviceId}/keys")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device keys.")
  public ResponseEntity downloadKeys(@PathVariable long deviceId)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException, IOException {
    // Prepare a filename for downloading.
    String filename =
      new Slugify().slugify(deviceService.findById(deviceId, true).getHardwareId()) + ".keys";

    //TODO update with remaining keys for the device (i.e. provisioning)

    // Get the keys and decrypt values.
    final DeviceKeyDTO deviceKeys = deviceService.findKeys(deviceId);
    deviceKeys
      .setPrivateKey(new String(securityService.decrypt(deviceKeys.getPrivateKey()),
        StandardCharsets.UTF_8));
    deviceKeys
      .setSessionKey(
        Base64E.encode(securityService.decrypt(deviceKeys.getSessionKey())));

    // Prepare the reply.
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PUBLIC KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPublicKey());
    stringBuilder.append("\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PRIVATE KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPrivateKey());
    stringBuilder.append("\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("SESSION KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getSessionKey());
    stringBuilder.append("\n\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PLATFORM PUBLIC KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPsPublicKey());

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(stringBuilder.toString());
  }

  @GetMapping(path = "fields", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get fields for measurement.")
  public List<FieldDTO> getFields() {
    return devicePageService.findAllSynthetic();
  }

  @PostMapping("fields")
  public ResponseEntity saveFields(@Valid @RequestBody List<FieldDTO> fields) {
    devicePageService.save(fields);
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "field-values/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device page fields.")
  public List<FieldDTO> getFieldValues(@PathVariable long id) {
    return devicePageService.findWithLatestValues(id);
  }
}
