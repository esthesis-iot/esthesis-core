package esthesis.platform.server.nifi.sinks.loggers.syslog;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PutSyslogConfiguration {

  @NotNull
  private String hostname;
  @NotNull
  private int port;
  @NotNull
  private String protocol;
  @NotNull
  private String messageBody;
  @NotNull
  private String messagePriority;

  private String keystoreFilename;
  private String keystorePassword;
  private String truststoreFilename;
  private String truststorePassword;

}
