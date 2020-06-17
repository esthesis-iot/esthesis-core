package esthesis.platform.server.nifi.sinks.producers.mysql;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExecuteSQLConfiguration {

  @NotNull
  private String databaseConnectionURL;
  @NotNull
  private String databaseDriverClassName;
  @NotNull
  private String databaseDriverClassLocation;
  @NotNull
  private String databaseUser;
  private String password;

  private String sqlPreQuery;
  private String sqlSelectQuery;
  private String sqlPostQuery;

}
