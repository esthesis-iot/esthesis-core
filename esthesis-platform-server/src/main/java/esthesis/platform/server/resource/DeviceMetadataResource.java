package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.datasink.dto.MetadataFieldDTO;
import esthesis.platform.server.service.DeviceMetadataService;
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

  private final DeviceMetadataService deviceMetadataService;

  public DeviceMetadataResource(DeviceMetadataService deviceMetadataService) {
    this.deviceMetadataService = deviceMetadataService;
  }

  @GetMapping(path = "{measurement}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get fields for measurement.")
  public List<MetadataFieldDTO> get(@PathVariable String measurement) {
    return deviceMetadataService.findAllSynthetic(measurement);
  }

  @PostMapping
  public ResponseEntity save(@Valid @RequestBody List<MetadataFieldDTO> fields) {
    deviceMetadataService.save(fields);
    return ResponseEntity.ok().build();
  }
}
