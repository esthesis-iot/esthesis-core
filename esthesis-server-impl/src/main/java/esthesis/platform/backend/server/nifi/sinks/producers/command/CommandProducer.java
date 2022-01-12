package esthesis.platform.backend.server.nifi.sinks.producers.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;
import esthesis.platform.backend.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.backend.server.nifi.client.services.NiFiClientService;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.ExistingProcessorSuffix;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.Type;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.backend.server.nifi.sinks.producers.NiFiProducerFactory;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

@RequiredArgsConstructor
@Component
public class CommandProducer implements NiFiProducerFactory {

  private static final String NAME = "CommandProducer";
  private static final String EXECUTE_SQL_NAME = "Find device ID from hardware ID";
  private static final String PUT_SQL_NAME = "Insert command request into the db";
  private static final String PUBLISH_MQTT_NAME = "PublishMQTT";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private CommandConfiguration conf;

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
    return
      "uri: \n" +
        "topic: \n" +
        "qos: \n" +
        "retainMesage: \n" +
        "keystoreFilename: \n" +
        "keystorePassword: \n" +
        "truststoreFilename: \n" +
        "truststorePassword:  \n" +
        "databaseConnectionURL: \n" +
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
    niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));

    String sqlExecutorId = niFiClientService
      .createExecuteSQL(EXECUTE_SQL_NAME, dbConnectionPool,
        conf.getSchedulingPeriod(), path
        , true);

    String putSQLId = niFiClientService
      .createPutSQL(PUT_SQL_NAME, dbConnectionPool, conf.getSchedulingPeriod(),
        path,
        true);

    enableControllerServices(dbConnectionPool);

    String sslContextId = null;
    String keystoreFilename = conf.getKeystoreFilename();
    String keystorePassword = conf.getKeystorePassword();
    String truststoreFilename = conf.getTruststoreFilename();
    String truststorePassword = conf.getTruststorePassword();

    if (ObjectUtils
      .allNotNull(keystoreFilename, keystorePassword, truststoreFilename, truststorePassword)) {

      sslContextId = niFiClientService.createSSLContext(niFiSinkDTO.getName() + " [SSL Context] ",
        keystoreFilename, keystorePassword, truststoreFilename, truststorePassword, path);

      customInfo.setSslContextId(sslContextId);
      niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
      enableControllerServices(sslContextId);
    }

    String mqttPublisherId = niFiClientService
      .createMQTTPublisher(PUBLISH_MQTT_NAME, conf.getUri(), conf.getTopic(),
        conf.getQos(),
        conf.isRetainMessage(), sslContextId, conf.getSchedulingPeriod(), path,
        true);

    createConnections(path, sqlExecutorId, putSQLId, mqttPublisherId);
    if (niFiSinkDTO.isState()) {
      niFiClientService.changeProcessorGroupState(path, STATE.RUNNING);
    }

    niFiClientService.manageQueueHandling(path, Type.EXECUTE_SQL);
  }

  @Override
  public void updateSink(NiFiSink sink,
    NiFiSinkDTO sinkDTO, String[] path) throws IOException {

    boolean isConfigurationChanged = !Objects
      .equals(sink.getConfiguration(), sinkDTO.getConfiguration());

    if (isConfigurationChanged) {
      CustomInfo customInfo = objectMapper.readValue(sink.getCustomInfo(), CustomInfo.class);
      CommandConfiguration prevConf = extractConfiguration(sink.getConfiguration());
      conf = extractConfiguration(sinkDTO.getConfiguration());

      updateDbProperties(customInfo, prevConf);
      String sslContextId = updateSSLProperties(sinkDTO, path, customInfo, prevConf,
        customInfo.getSslContextId());

      boolean isSchedulingPeriodUpdated = !Objects
        .equals(prevConf.getSchedulingPeriod(), conf.getSchedulingPeriod());

      if (isSchedulingPeriodUpdated) {
        String executeSQLProcessorId =
          niFiClientService.findProcessorIDByNameAndProcessGroup(EXECUTE_SQL_NAME,
            path);
        niFiClientService.updateExecuteSQL(executeSQLProcessorId, EXECUTE_SQL_NAME,
          conf.getSchedulingPeriod());

        String putSQLProcessorId = niFiClientService
          .findProcessorIDByNameAndProcessGroup(PUT_SQL_NAME,
            path);
        niFiClientService.updatePutSQL(putSQLProcessorId, PUT_SQL_NAME,
          conf.getSchedulingPeriod());
      }

      boolean isMQTTUpdated = !(Objects.equals(conf.getUri(), prevConf.getUri()) &&
        Objects.equals(conf.getQos(), prevConf.getQos()) &&
        Objects.equals(conf.getTopic(), prevConf.getTopic()) &&
        Objects.equals(conf.isRetainMessage(), prevConf.isRetainMessage()));

      if (isSchedulingPeriodUpdated || isMQTTUpdated) {
        String publishMQTTProcessorId =
          niFiClientService.findProcessorIDByNameAndProcessGroup(PUBLISH_MQTT_NAME,
            path);
        niFiClientService
          .updatePublisherMQTT(publishMQTTProcessorId, PUBLISH_MQTT_NAME, sslContextId, conf.getUri(),
            conf.getTopic(),
            conf.getQos(), conf.isRetainMessage(), conf.getSchedulingPeriod());
      }
    }
  }

  private String updateSSLProperties(NiFiSinkDTO sinkDTO, String[] path, CustomInfo customInfo,
    CommandConfiguration prevConf, String sslContextId) throws IOException {
    if (!(Objects.equals(conf.getKeystoreFilename(), prevConf.getKeystoreFilename()) &&
      Objects.equals(conf.getKeystorePassword(), prevConf.getKeystorePassword()) &&
      Objects.equals(conf.getTruststoreFilename(), prevConf.getTruststoreFilename()) &&
      Objects.equals(conf.getTruststorePassword(), prevConf.getTruststorePassword()))) {

      if (!StringUtils.hasText(sslContextId)) {
        sslContextId = niFiClientService
          .createSSLContextForExistingProcessor(sinkDTO.getName(), path,
            conf.getKeystoreFilename(), conf.getKeystorePassword(), conf.getTruststoreFilename(),
            conf.getTruststorePassword());

        customInfo.setSslContextId(sslContextId);
        sinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
        enableControllerServices(sslContextId);
      } else {
        niFiClientService
          .updateSSLContext(customInfo.getSslContextId(), conf.getKeystoreFilename(),
            conf.getKeystorePassword(), conf.getTruststoreFilename(), conf.getTruststorePassword());
      }
    }
    return sslContextId;
  }

  private void updateDbProperties(CustomInfo customInfo, CommandConfiguration prevConf)
    throws IOException {
    if (!(conf.getDatabaseConnectionURL().equals(prevConf.getDatabaseConnectionURL())
      && conf.getDatabaseDriverClassName().equals(prevConf.getDatabaseDriverClassName())
      && conf.getDatabaseDriverClassLocation().equals(prevConf.getDatabaseDriverClassLocation())
      && conf.getDatabaseUser().equals(prevConf.getDatabaseUser())
      && conf.getPassword().equals(prevConf.getPassword()))) {

      niFiClientService
        .updateDBCConnectionPool(customInfo.getDbConnectionPool(), conf.getDatabaseConnectionURL(),
          conf.getDatabaseDriverClassName(),
          conf.getDatabaseDriverClassLocation(),
          conf.getDatabaseUser(),
          conf.getPassword());
    }
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.changeProcessorGroupState(path, STATE.STOPPED);
    CustomInfo customInfo = objectMapper.readValue(niFiSinkDTO.getCustomInfo(), CustomInfo.class);
    niFiClientService.deleteProcessor(EXECUTE_SQL_NAME, path);
    niFiClientService.deleteProcessor(PUT_SQL_NAME, path);
    niFiClientService.deleteProcessor(PUBLISH_MQTT_NAME, path);
    niFiClientService.manageQueueHandling(path, Type.EXECUTE_SQL);
    deleteControllerServices(customInfo);
  }

  private void deleteControllerServices(CustomInfo customInfo) throws IOException {
    if (customInfo != null) {
      if (StringUtils.hasText(customInfo.getDbConnectionPool())) {
        niFiClientService.deleteController(customInfo.getDbConnectionPool());
      }
      if (StringUtils.hasText(customInfo.getSslContextId())) {
        niFiClientService.deleteController(customInfo.getSslContextId());
      }
    }
  }

  @Override
  public void toggleSink(String name, String[] path, boolean isEnabled) throws IOException {
    niFiClientService.changeProcessorGroupState(path, isEnabled ? STATE.RUNNING : STATE.STOPPED);
  }

  @Override
  public void enableControllerServices(String... controllerServices) throws IOException {
    for (String id : controllerServices) {
      niFiClientService.changeControllerServiceStatus(id, STATE.ENABLED);
    }
  }

  @Override
  public String getSinkValidationErrors(String name, String[] path) throws IOException {
    return StringUtils
      .trimAllWhitespace(niFiClientService.getValidationErrors(EXECUTE_SQL_NAME,
        path) + "\n" + niFiClientService.getValidationErrors(PUT_SQL_NAME, path)
        + "\n" + niFiClientService.getValidationErrors(PUBLISH_MQTT_NAME, path));
  }

  @Override
  public boolean exists(String name, String[] path) throws IOException {
    return niFiClientService.processorExists(PUBLISH_MQTT_NAME, path) &&
      niFiClientService.processorExists(PUT_SQL_NAME, path) &&
      niFiClientService.processorExists(EXECUTE_SQL_NAME, path);
  }

  @Override
  public boolean isSinkRunning(String name, String[] path) throws IOException {
    return niFiClientService.isProcessorRunning(PUBLISH_MQTT_NAME, path) &&
      niFiClientService.isProcessorRunning(PUT_SQL_NAME, path) &&
      niFiClientService.isProcessorRunning(EXECUTE_SQL_NAME, path);
  }

  private CommandConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    return new Yaml(new Constructor(CommandConfiguration.class),
      representer)
      .load(configuration);
  }

  private void createConnections(String[] path, String sqlExecutorId, String putSQLId,
    String mqttPublisherId) throws IOException {

    //executeSQL
    Set<String> relationship = new HashSet<>(
        List.of(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));

    String postBodyCleanerId = niFiClientService.findProcessorIDByNameAndProcessGroup(
        ExistingProcessorSuffix.CLEAR_POST_BODY, path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);
    String sqlResultConverterId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      ExistingProcessorSuffix.CONVERT_SQL_RESULT, path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);

    niFiClientService.connectComponentsInSameGroup(path, postBodyCleanerId,
      sqlExecutorId, relationship);
    niFiClientService
      .connectComponentsInSameGroup(path, sqlExecutorId, sqlResultConverterId,
        relationship);

    niFiClientService.moveComponent(path, sqlExecutorId);

    //puSQL
    relationship = new HashSet<>(
        List.of(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));

    String sqlResultBodyCleanerId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      ExistingProcessorSuffix.CLEAR_SQL_RESULT_BODY, path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);
    String mqttCommandBodyGeneratorId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      ExistingProcessorSuffix.GENERATE_MQTT_COMMAND_BODY, path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);

    niFiClientService
      .connectComponentsInSameGroup(path, sqlResultBodyCleanerId, putSQLId, relationship);
    niFiClientService
      .connectComponentsInSameGroup(path, putSQLId, mqttCommandBodyGeneratorId, relationship);

    niFiClientService.moveComponent(path, putSQLId);

    //publishMQTT
    relationship = new HashSet<>(
        List.of(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));

    String commandIdResponseSetterId = niFiClientService
      .findProcessorIDByNameAndProcessGroup(ExistingProcessorSuffix.SET_COMMAND_ID_RESPONSE, path,
        NiFiSearchAlgorithm.NAME_ENDS_WITH);

    niFiClientService
      .connectComponentsInSameGroup(path, mqttCommandBodyGeneratorId, mqttPublisherId,
        relationship);
    niFiClientService
      .connectComponentsInSameGroup(path, mqttPublisherId, commandIdResponseSetterId, relationship);
    niFiClientService.moveComponent(path, mqttPublisherId);
  }
}
