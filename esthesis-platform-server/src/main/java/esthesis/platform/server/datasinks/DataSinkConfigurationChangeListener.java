package esthesis.platform.server.datasinks;

import esthesis.platform.server.events.DataSinkConfigurationChangedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DataSinkConfigurationChangeListener implements ApplicationListener<DataSinkConfigurationChangedEvent> {

  @Override
  public void onApplicationEvent(DataSinkConfigurationChangedEvent event) {

  }

}
