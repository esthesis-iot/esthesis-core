package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.exception.QMismatchException;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.common.util.Base64E;
import esthesis.platform.backend.server.dto.DeviceKeyDTO;
import esthesis.platform.backend.server.dto.DevicePageDTO;
import esthesis.platform.backend.server.dto.DeviceRegistrationDTO;
import esthesis.platform.backend.server.model.Device;
import esthesis.platform.backend.server.service.DeviceService;
import esthesis.platform.backend.server.service.SecurityService;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  private final DeviceService deviceService;
  private final SecurityService securityService;

  public DevicesResource(DeviceService deviceService, SecurityService securityService) {
    this.deviceService = deviceService;
    this.securityService = securityService;
  }

  @PostMapping(path = "preregister")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device(s).")
  public ResponseEntity preregister(@Valid @RequestBody DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, IOException, NoSuchPaddingException,
         InvalidAlgorithmParameterException, InvalidKeyException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
    deviceService.preregister(deviceRegistrationDTO);

    return ResponseEntity.ok().build();
  }

  @EmptyPredicateCheck
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  @ReplyPageableFilter("createdOn,hardwareId,id,state,lastSeen")
  public Page<DeviceDTO> findAll(@QuerydslPredicate(root = Device.class) Predicate predicate,
    Pageable pageable) {
    return deviceService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device.")
  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public DeviceDTO get(@PathVariable long id) {
    return deviceService.findById(id, true);
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
  @SuppressWarnings("java:S1192")
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
    // TODO switch to a JSON reply
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

  @GetMapping(path = "device-page-data/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not fetch device page fields.")
  @ReplyFilter("-shown,-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DevicePageDTO> getDevicePageData(@PathVariable long deviceId) {
    return deviceService.getDevicePageData(deviceId);
  }

  /**
   * Returns the last value of a specific telemetry or metadata field for a device.
   *
   * @param deviceId The Id of the device to fetch the field value for.
   * @param fields The name of the telemetry or metadata field to fetch. The field needs to follow
   * the following format: TYPE.MEASUREMENT.FIELD For example, TELEMETRY.geolocation.latitude.
   * Multiple fields can be requested separated by comma.
   */
  @GetMapping(path = "device-data-field/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not fetch device page field.")
  @ReplyFilter("-shown,-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DevicePageDTO> getDeviceDataFields(@PathVariable long deviceId,
    @RequestParam String fields) {
    List<DevicePageDTO> results = new ArrayList<>();
    for (String field : fields.split(",")) {
      if (field.split("\\.").length < 3) {
        throw new QMismatchException("Unsupported field name format.");
      }
      results.add(deviceService.getDeviceDataField(deviceId, field));
    }

    return results;
  }

  @GetMapping(path = "count/by-hardware-id", produces = MediaType.APPLICATION_JSON_VALUE)
  public int countByHardwareId(@RequestParam String hardwareIds) {
    if (StringUtils.isBlank(hardwareIds)) {
      return 0;
    } else {
      return deviceService.countByHardwareIds(Arrays.asList(hardwareIds.split(",")));
    }
  }

  @GetMapping(path = "count/by-tags", produces = MediaType.APPLICATION_JSON_VALUE)
  public int countByTags(@RequestParam String tags) {
    return deviceService.countByTags(Arrays.asList(tags.split(",")));
  }

  @GetMapping(path = "/by-partial-hardware-id/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not search for device.")
  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public List<DeviceDTO> findByPartialHardwareId(@PathVariable String hardwareId) {
    return deviceService.findByPartialHardwareId(hardwareId);
  }
}
