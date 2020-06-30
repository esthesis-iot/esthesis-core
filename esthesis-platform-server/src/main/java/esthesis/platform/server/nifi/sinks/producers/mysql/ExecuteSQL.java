package esthesis.platform.server.nifi.sinks.producers.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.producers.NiFiProducerFactory;
import esthesis.platform.server.nifi.sinks.writers.relational.CustomInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class ExecuteSQL implements NiFiProducerFactory {

  private final static String NAME = "ExecuteSQL";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private ExecuteSQLConfiguration conf;

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public boolean supportsTelemetryProduce() {
    return true;
  }

  @Override
  public String getConfigurationTemplate() {
    return "databaseConnectionURL: \n" +
      "databaseDriverClassName: \n" +
      "databaseDriverClassLocation: \n" +
      "databaseUser: \n" +
      "password: \n" +
      "sqlPreQuery: \n" +
      "sqlSelectQuery: \n" +
      "sqlPostQuery: ";
  }

  @Override
  public NiFiSinkDTO createSink(
    NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String dbConnectionPool = niFiClientService
      .createDBConnectionPool(niFiSinkDTO.getName() + " [DB CONNECTION POOL] ",
        conf.getDatabaseConnectionURL(),
        conf.getDatabaseDriverClassName(),
        conf.getDatabaseDriverClassLocation(),
        conf.getDatabaseUser(),
        conf.getPassword(), path);

    CustomInfo customInfo = new CustomInfo();
    customInfo.setDbConnectionPool(dbConnectionPool);
    niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));

    String executeSQL = niFiClientService.createExecuteSQL(niFiSinkDTO.getName(), dbConnectionPool,
      conf.getSqlPreQuery(), conf.getSqlSelectQuery(), conf.getSqlPostQuery(), path);

    niFiSinkDTO.setProcessorId(executeSQL);
    enableControllerServices(dbConnectionPool);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {

    ExecuteSQLConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());

    if (!(conf.getDatabaseConnectionURL().equals(prevConf.getDatabaseConnectionURL())
      && conf.getDatabaseDriverClassName().equals(prevConf.getDatabaseDriverClassName())
      && conf.getDatabaseDriverClassLocation().equals(prevConf.getDatabaseDriverClassLocation())
      && conf.getDatabaseUser().equals(prevConf.getDatabaseUser())
      && conf.getPassword().equals(prevConf.getPassword()))) {

      CustomInfo customInfo = objectMapper.readValue(sink.getCustomInfo(), CustomInfo.class);
      niFiClientService
        .updateDBCConnectionPool(customInfo.getDbConnectionPool(), conf.getDatabaseConnectionURL(),
          conf.getDatabaseDriverClassName(),
          conf.getDatabaseDriverClassLocation(),
          conf.getDatabaseUser(),
          conf.getPassword());
    }

    return niFiClientService.updateExecuteSQL(sink.getProcessorId(),
      conf.getSqlPreQuery(), conf.getSqlSelectQuery(), conf.getSqlPostQuery());
  }

  @Override
  public String deleteSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
    return niFiClientService.deleteProcessor(niFiSinkDTO.getProcessorId());
  }

  @Override
  public String toggleSink(String id, boolean isEnabled) throws IOException {
    return niFiClientService.changeProcessorStatus(id, isEnabled ? STATE.RUNNING : STATE.STOPPED);
  }

  @Override
  public void enableControllerServices(String... controllerServices) throws IOException {
    for (String id : controllerServices) {
      niFiClientService.changeControllerServiceStatus(id, STATE.ENABLED);
    }
  }

  @Override
  public String getSinkValidationErrors(String id) throws IOException {
    return niFiClientService.getValidationErrors(id);
  }

  @Override
  public boolean exists(String id) throws IOException {
    return niFiClientService.processorExists(id);
  }

  private ExecuteSQLConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(ExecuteSQLConfiguration.class),
      representer)
      .load(configuration);
  }
}
