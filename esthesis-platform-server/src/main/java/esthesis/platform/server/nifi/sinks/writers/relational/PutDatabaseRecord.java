package esthesis.platform.server.nifi.sinks.writers.relational;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppConstants.NIFI_SINK_HANDLER;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATEMENT_TYPE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.server.nifi.sinks.writers.NiFiWriterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PutDatabaseRecord implements NiFiWriterFactory {

  private static final String NAME = "Relational";
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
    return true;
  }

  @Override
  public boolean supportsCommandWrite() {
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
        "password: \n" +
        "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    boolean isCommandHandler = niFiSinkDTO.getHandler() == NIFI_SINK_HANDLER.COMMAND.getType();

    deleteControllerServices(niFiSinkDTO);
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

    String putDatabaseRecordId = niFiClientService
      .createPutDatabaseRecord(niFiSinkDTO.getName(), jsonTreeReader, dbConnectionPool,
        getStatementType(niFiSinkDTO.getHandler()), conf.getSchedulingPeriod(), path,
        isCommandHandler);

    CustomInfo customInfo = new CustomInfo();
    customInfo.setJsonTreeReader(jsonTreeReader);
    customInfo.setDbConnectionPool(dbConnectionPool);
    niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));

    enableControllerServices(jsonTreeReader, dbConnectionPool);

    if(isCommandHandler) {
      Set<String> relationship = new HashSet<>(
        Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));

      String prepareDatabaseOperationId = niFiClientService.findProcessorIDByNameAndProcessGroup(
        "[PDO]", path,
        NiFiSearchAlgorithm.NAME_ENDS_WITH);

      niFiClientService.connectComponentsInSameGroup(path, prepareDatabaseOperationId, putDatabaseRecordId
       ,relationship);

      niFiClientService.changeProcessorGroupState(path, STATE.RUNNING);

      if (!niFiSinkDTO.isState()) {
        niFiClientService.changeProcessorStatus(niFiSinkDTO.getName(), path, STATE.STOPPED);
      }
    }
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    PutDatabaseRecordConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());

    if (!(Objects.equals(conf.getDatabaseConnectionURL(), prevConf.getDatabaseConnectionURL())
      && Objects.equals(conf.getDatabaseDriverClassName(), prevConf.getDatabaseDriverClassName())
      && Objects.equals(conf.getDatabaseDriverClassLocation(),
      prevConf.getDatabaseDriverClassLocation())
      && Objects.equals(conf.getDatabaseUser(), prevConf.getDatabaseUser())
      && Objects.equals(conf.getPassword(), prevConf.getPassword()))) {

      CustomInfo customInfo = objectMapper.readValue(sink.getCustomInfo(), CustomInfo.class);
      niFiClientService
        .updateDBCConnectionPool(customInfo.getDbConnectionPool(), conf.getDatabaseConnectionURL(),
          conf.getDatabaseDriverClassName(),
          conf.getDatabaseDriverClassLocation(),
          conf.getDatabaseUser(),
          conf.getPassword());
    }

    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    return niFiClientService.updatePutDatabaseRecord(processorId, sinkDTO.getName(),
      getStatementType(sinkDTO.getHandler()), conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    if (niFiSinkDTO.getHandler() == NIFI_SINK_HANDLER.COMMAND.getType()) {
      niFiClientService.changeProcessorGroupState(path, STATE.STOPPED);
    }
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    deleteControllerServices(niFiSinkDTO);
  }

  private void deleteControllerServices(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper.readValue(customInfoString, CustomInfo.class);
      niFiClientService.deleteController(customInfo.getJsonTreeReader());
      niFiClientService.deleteController(customInfo.getDbConnectionPool());
    }
  }

  @Override
  public String toggleSink(String name, String[] path, boolean isEnabled) throws IOException {
    return niFiClientService.changeProcessorStatus(name, path,
      isEnabled ? STATE.RUNNING : STATE.STOPPED);
  }

  @Override
  public void enableControllerServices(String... controllerServices) throws IOException {
    for (String id : controllerServices) {
      niFiClientService.changeControllerServiceStatus(id, STATE.ENABLED);
    }
  }

  @Override
  public String getSinkValidationErrors(String name, String[] path) throws IOException {
    return niFiClientService.getValidationErrors(name, path);
  }

  @Override
  public boolean exists(String name, String[] path) throws IOException {
    return niFiClientService.processorExists(name, path);
  }

  @Override
  public boolean isSinkRunning(String name, String[] path) throws IOException {
    return niFiClientService.isProcessorRunning(name, path);
  }

  private PutDatabaseRecordConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutDatabaseRecordConfiguration.class),
      representer)
      .load(configuration);
  }

  private STATEMENT_TYPE getStatementType(int handler) {
    NIFI_SINK_HANDLER nifiSinkHandler = NIFI_SINK_HANDLER.valueOf(handler);
    switch (nifiSinkHandler) {
      case TELEMETRY:
      case COMMAND:
        return STATEMENT_TYPE.INSERT;
      case METADATA:
        return STATEMENT_TYPE.UPDATE;
      default:
        return STATEMENT_TYPE.USE_STATE_ATTRIBUTE;
    }
  }
}
