package esthesis.platform.backend.server.nifi.sinks.producers.command;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandConfiguration {

  private String schedulingPeriod;

  @NotNull
  private String databaseConnectionURL;
  @NotNull
  private String databaseDriverClassName;
  @NotNull
  private String databaseDriverClassLocation;
  @NotNull
  private String databaseUser;
  private String password;

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

}
