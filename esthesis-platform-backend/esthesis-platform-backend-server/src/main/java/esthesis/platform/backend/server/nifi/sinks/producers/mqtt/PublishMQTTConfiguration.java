package esthesis.platform.backend.server.nifi.sinks.producers.mqtt;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublishMQTTConfiguration {

  @NotNull
  private String uri;
  @NotNull
  private String topic;
  @NotNull
  private int qos;
  @NotNull
  private boolean retainMessage;

  private String keystoreFilename;
  private String keystorePassword;
  private String truststoreFilename;
  private String truststorePassword;
  private String schedulingPeriod;
}
