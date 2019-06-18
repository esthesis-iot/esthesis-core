package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.datasink.dto.FieldDTO;
import esthesis.platform.server.service.DevicePageService;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/device-metadata")
public class DeviceMetadataResource {

  private final DevicePageService deviceMetadataService;

  public DeviceMetadataResource(DevicePageService deviceMetadataService) {
    this.deviceMetadataService = deviceMetadataService;
  }

  @GetMapping(path = "{measurement}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get fields for measurement.")
  public List<FieldDTO> get(@PathVariable String measurement) {
    return deviceMetadataService.findAllSynthetic(measurement);
  }

  @PostMapping
  public ResponseEntity save(@Valid @RequestBody List<FieldDTO> fields) {
    deviceMetadataService.save(fields);
    return ResponseEntity.ok().build();
  }
}
