package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.service.DeviceService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  private final DeviceService deviceService;

  public DevicesResource(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @PostMapping(value = "/preregister")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device(s).")
  public ResponseEntity preregister(@Valid @RequestBody DeviceRegistrationDTO deviceRegistrationDTO) {
    // Split ids, first by commas then by new lines.
    String ids = deviceRegistrationDTO.getIds();
    if (ids.contains(",")) {
      deviceService.preregister(ids.split(","));
    } else {
      deviceService.preregister(ids.split("\n"));
    }

    return ResponseEntity.ok().build();
  }


  @EmptyPredicateCheck
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
//  @ReplyPageableFilter("createdOn,deviceId,id,status,tags")
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
  public RegistrationResponse registerDevice(@Valid @RequestBody RegistrationRequest registrationRequest) {
    final RegistrationResponse response = new RegistrationResponse();

    try {
      final RegistrationResponse registrationResponse = deviceService.register(registrationRequest);
      response.setPublicKey(registrationResponse.getPublicKey());
      response.setPrivateKey(registrationResponse.getPrivateKey());
    } catch (Exception e) {
      response.setError(true);
      response.setErrorCode(e.getMessage());
    }

    return response;
  }

}
