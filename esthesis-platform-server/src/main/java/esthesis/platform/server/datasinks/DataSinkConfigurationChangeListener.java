package esthesis.platform.server.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.platform.server.service.DataSinkService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DataSinkConfigurationChangeListener implements ApplicationListener<MQTTDataEvent> {
  private final DataSinkService dataSinkService;

  public DataSinkConfigurationChangeListener(DataSinkService dataSinkService) {
    this.dataSinkService = dataSinkService;
  }

  @Override
  public void onApplicationEvent(MQTTDataEvent event) {

  }

}
