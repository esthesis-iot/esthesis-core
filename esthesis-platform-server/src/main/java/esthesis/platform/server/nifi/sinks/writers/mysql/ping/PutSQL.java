package esthesis.platform.server.nifi.sinks.writers.mysql.ping;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.writers.NiFiWriterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class PutSQL implements NiFiWriterFactory {

  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private final static String NAME = "PutSQL";
  private PutSQLConfiguration conf;

  @Override
  public boolean supportsMetadataWrite() {
    return false;
  }

  @Override
  public boolean supportsTelemetryWrite() {
    return false;
  }

  @Override
  public boolean supportsPingWrite() {
    return true;
  }

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public String getConfigurationTemplate() {
    return
      "databaseConnectionURL: \n" +
        "databaseDriverClassName: \n" +
        "databaseDriverClassLocation: \n" +
        "databaseUser: \n" +
        "sqlStatement: ";
  }

  @Override
  public NiFiSinkDTO createSink(
    NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    deleteControllerServices(niFiSinkDTO);
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String jdbcServiceId = niFiClientService
      .createDBConnectionPool(niFiSinkDTO.getName() + " [JDBC Connection Pool] ",
        conf.getDatabaseConnectionURL(),
        conf.getDatabaseDriverClassName(),
        conf.getDatabaseDriverClassLocation(),
        conf.getDatabaseUser(),
        conf.getPassword(), path);

    String putDatabaseRecord = niFiClientService
      .createPutSQL(niFiSinkDTO.getName(), jdbcServiceId, conf.getSqlStatement(), path);

    CustomInfo customInfo = new CustomInfo();
    customInfo.setJdbcServiceId(jdbcServiceId);
    niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));

    niFiSinkDTO.setProcessorId(putDatabaseRecord);
    enableControllerServices(jdbcServiceId);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    PutSQLConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());

    if (!(Objects.equals(conf.getDatabaseConnectionURL(), prevConf.getDatabaseConnectionURL())
      && Objects.equals(conf.getDatabaseDriverClassName(), prevConf.getDatabaseDriverClassName())
      && Objects.equals(conf.getDatabaseDriverClassLocation(),
      prevConf.getDatabaseDriverClassLocation())
      && Objects.equals(conf.getDatabaseUser(), prevConf.getDatabaseUser())
      && Objects.equals(conf.getPassword(), prevConf.getPassword()))) {

      CustomInfo customInfo = objectMapper
        .readValue(sink.getCustomInfo(), CustomInfo.class);
      niFiClientService
        .updateDBCConnectionPool(customInfo.getJdbcServiceId(), conf.getDatabaseConnectionURL(),
          conf.getDatabaseDriverClassName(),
          conf.getDatabaseDriverClassLocation(),
          conf.getDatabaseUser(),
          conf.getPassword());
    }

    return niFiClientService.updatePutSQL(sink.getProcessorId(), conf.getSqlStatement());
  }

  @Override
  public String deleteSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String id = niFiClientService.deleteProcessor(niFiSinkDTO.getProcessorId());
    deleteControllerServices(niFiSinkDTO);
    return id;
  }

  private void deleteControllerServices(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper.readValue(customInfoString, CustomInfo.class);
      niFiClientService.deleteController(customInfo.getJdbcServiceId());
    }
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

  private PutSQLConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutSQLConfiguration.class),
      representer)
      .load(configuration);
  }
}
