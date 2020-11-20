package esthesis.backend.nifi.sinks.readers.mqtt;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsumeMQTTConfiguration {

  @NotNull
  private String uri;
  @NotNull
  private String topic;
  @NotNull
  private int qos;
  private int queueSize = 1000;

  private String keystoreFilename;
  private String keystorePassword;
  private String truststoreFilename;
  private String truststorePassword;
  private String schedulingPeriod;
}
