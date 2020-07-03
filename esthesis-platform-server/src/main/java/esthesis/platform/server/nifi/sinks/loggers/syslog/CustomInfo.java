package esthesis.platform.server.nifi.sinks.loggers.syslog;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomInfo {

  private String sslContextId;

}
