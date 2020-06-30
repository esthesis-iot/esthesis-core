package esthesis.platform.server.nifi.sinks.writers.relational;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomInfo {

  private String jsonTreeReader;
  private String dbConnectionPool;

}
