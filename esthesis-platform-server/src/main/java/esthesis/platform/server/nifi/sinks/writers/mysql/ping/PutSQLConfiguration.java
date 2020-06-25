package esthesis.platform.server.nifi.sinks.writers.mysql.ping;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PutSQLConfiguration {

  @NotNull
  private String databaseConnectionURL;
  @NotNull
  private String databaseDriverClassName;
  @NotNull
  private String databaseDriverClassLocation;
  @NotNull
  private String databaseUser;
  private String password;
  private String sqlStatement;

}
