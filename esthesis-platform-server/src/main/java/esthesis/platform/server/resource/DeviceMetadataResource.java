package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.datasink.config.AppConstants.Mqtt.EventType;
import esthesis.extension.datasink.dto.FieldDTO;
import esthesis.platform.server.service.DTService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/device-metadata")
public class DeviceMetadataResource {
  private final DTService dtService;

  public DeviceMetadataResource(DTService dtService) {
    this.dtService = dtService;
  }

  @GetMapping(path = "{measurement}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get fiels for measurement.")
  public List<FieldDTO> get(@PathVariable String measurement) {
    return dtService.getFieldsForMeasurement(EventType.METADATA, measurement);
  }
}
