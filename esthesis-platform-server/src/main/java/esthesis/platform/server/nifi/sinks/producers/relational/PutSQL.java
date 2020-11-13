package esthesis.platform.server.nifi.sinks.producers.relational;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.server.nifi.sinks.producers.NiFiProducerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PutSQL implements NiFiProducerFactory {

  private static final String NAME = "PutSQL";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private RelationProducerConfiguration conf;

  @Override
  public boolean supportsTelemetryProduce() {
    return false;
  }

  @Override
  public boolean supportsMetadataProduce() {
    return false;
  }

  @Override
  public boolean supportsCommandProduce() {
    return true;
  }

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public String getConfigurationTemplate() {
    return "databaseConnectionURL: \n" +
      "databaseDriverClassName: \n" +
      "databaseDriverClassLocation: \n" +
      "databaseUser: \n" +
      "password:  \n" +
      "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO,
    String[] path) throws IOException {

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
    niFiSinkDTO
      .setCustomInfo(objectMapper.writeValueAsString(customInfo));

    String putSQLId = niFiClientService
      .createPutSQL(niFiSinkDTO.getName(), dbConnectionPool, conf.getSchedulingPeriod(), path,
        true);

    enableControllerServices(dbConnectionPool);

    Set<String> relationship = new HashSet<>(
      Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));

    String sqlResultBodyCleanerId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      "[CSRB]", path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);
    String mqttCommandBodyGeneratorId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      "[GMCB]", path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);

    niFiClientService.connectComponentsInSameGroup(path, sqlResultBodyCleanerId, putSQLId, relationship);
    niFiClientService.connectComponentsInSameGroup(path, putSQLId, mqttCommandBodyGeneratorId,relationship);

    niFiClientService.moveComponent(path, putSQLId);

    niFiClientService.changeProcessorGroupState(path, STATE.RUNNING);

    if (!niFiSinkDTO.isState()) {
      niFiClientService.changeProcessorStatus(niFiSinkDTO.getName(), path, STATE.STOPPED);
    }
  }

  @Override
  public String updateSink(NiFiSink sink,
    NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    RelationProducerConfiguration prevConf = extractConfiguration(sink.getConfiguration());
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

    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    return niFiClientService.updatePutSQL(processorId, sinkDTO.getName(),
      conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.changeProcessorGroupState(path, STATE.STOPPED);
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    deleteControllerServices(niFiSinkDTO);
    niFiClientService.distributeLoadOfProducers(niFiSinkDTO.getHandler(), true);
  }

  private void deleteControllerServices(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper.readValue(customInfoString, CustomInfo.class);
      if (!StringUtils.isEmpty(customInfo.getDbConnectionPool())) {
        niFiClientService.deleteController(customInfo.getDbConnectionPool());
      }
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

  private RelationProducerConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(RelationProducerConfiguration.class),
      representer)
      .load(configuration);
  }
}
