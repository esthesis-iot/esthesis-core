package esthesis.platform.server.nifi.sinks.writers.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATEMENT_TYPE;
import esthesis.platform.server.nifi.sinks.writers.NiFiWriterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class PutDatabaseRecord implements NiFiWriterFactory {

  private final static String NAME = "PutDatabaseRecord";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private PutDatabaseRecordConfiguration conf;

  @Override
  public boolean supportsMetadataWrite() {
    return true;
  }

  @Override
  public boolean supportsTelemetryWrite() {
    return true;
  }

  @Override
  public boolean supportsPingWrite() {
    return false;
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
        "password: \n" +
        "statementType: \n" +
        "tableName: ";
  }

  @Override
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String jsonTreeReader = niFiClientService
      .createJsonTreeReader(niFiSinkDTO.getName() + " [JSON TREE READER] ", path);

    String dbConnectionPool = niFiClientService
      .createDBConnectionPool(niFiSinkDTO.getName() + " [DB CONNECTION POOL] ",
        conf.getDatabaseConnectionURL(),
        conf.getDatabaseDriverClassName(),
        conf.getDatabaseDriverClassLocation(),
        conf.getDatabaseUser(),
        conf.getPassword(), path);

    String putDatabaseRecord = niFiClientService
      .createPutDatabaseRecord(niFiSinkDTO.getName(), jsonTreeReader, dbConnectionPool,
        getStatementType(conf.getStatementType()), conf.getTableName(), path);

    CustomInfo customInfo = new CustomInfo();
    customInfo.setJsonTreeReader(jsonTreeReader);
    customInfo.setDbConnectionPool(dbConnectionPool);
    niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));

    niFiSinkDTO.setProcessorId(putDatabaseRecord);
    enableControllerServices(jsonTreeReader, dbConnectionPool);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    PutDatabaseRecordConfiguration prevConf = extractConfiguration(sink.getConfiguration());
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

    return niFiClientService.updatePutDatabaseRecord(sink.getProcessorId(),
      getStatementType(conf.getStatementType()), conf.getTableName());
  }

  @Override
  public String deleteSink(String id) throws IOException {
    return niFiClientService.deleteProcessor(id);
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

  private PutDatabaseRecordConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutDatabaseRecordConfiguration.class),
      representer)
      .load(configuration);
  }

  private STATEMENT_TYPE getStatementType(String statementType) {
    return statementType != null ? STATEMENT_TYPE.valueOf(statementType.toUpperCase()) :
      STATEMENT_TYPE.INSERT;
  }
}
