package esthesis.platform.server.nifi.sinks.writers.mysql.ping;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomInfo {

  private String jdbcServiceId;

}
