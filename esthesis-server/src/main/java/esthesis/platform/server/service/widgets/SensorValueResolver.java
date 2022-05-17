package esthesis.platform.server.service.widgets;

import com.eurodyn.qlack.common.exception.QMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.model.DashboardWidget;
import esthesis.platform.server.service.DeviceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
@RequiredArgsConstructor
public class SensorValueResolver implements WidgetValueResolver {

  private final ObjectMapper mapper;
  private final DeviceService deviceService;

  @Override
  public Object getValue(DashboardWidget widget) {
    try {
      SensorValueConf conf = mapper.readValue(widget.getConfiguration(),
        SensorValueConf.class);
      return deviceService.getDeviceDataField(
        deviceService.findByHardwareId(conf.getHardwareId()).getId(),
        conf.getMeasurement()).getValue();
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not obtain configuration for widget Id ''{0}''",
        widget.getId());
    }
  }

  @Data
  private static class SensorValueConf {

    private String hardwareId;
    private String measurement;
  }
}
