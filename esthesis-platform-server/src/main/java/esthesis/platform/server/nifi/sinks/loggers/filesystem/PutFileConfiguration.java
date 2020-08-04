package esthesis.platform.server.nifi.sinks.loggers.filesystem;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PutFileConfiguration {

  @NotNull
  private String directory;
  private String schedulingPeriod;

}
