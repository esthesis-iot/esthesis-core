package esthesis.platform.server.nifi.sinks.readers.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppConstants.NIFI_SINK_HANDLER;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Processor.ExistingProcessorSuffix;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.server.nifi.sinks.readers.NiFiReaderFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ConsumeMQTT implements NiFiReaderFactory {

  private static final String NAME = "ConsumeMQTT";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private ConsumeMQTTConfiguration conf;

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public boolean supportsPingRead() {
    return true;
  }

  @Override
  public boolean supportsMetadataRead() {
    return true;
  }

  @Override
  public boolean supportsTelemetryRead() {
    return true;
  }

  @Override
  public boolean supportsCommandRead() {
    return true;
  }

  public String getConfigurationTemplate() {
    return
      "uri: \n" +
        "topic: \n" +
        "qos: \n" +
        "queueSize: \n" +
        "keystoreFilename: \n" +
        "keystorePassword: \n" +
        "truststoreFilename: \n" +
        "truststorePassword:  \n" +
        "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    boolean isCommandHandler = niFiSinkDTO.getHandler() == NIFI_SINK_HANDLER.COMMAND.getType();
    deleteControllerServices(niFiSinkDTO);
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String sslContextId = null;
    String keystoreFilename = conf.getKeystoreFilename();
    String keystorePassword = conf.getKeystorePassword();
    String truststoreFilename = conf.getTruststoreFilename();
    String truststorePassword = conf.getTruststorePassword();

    if (ObjectUtils
      .allNotNull(keystoreFilename, keystorePassword, truststoreFilename, truststorePassword)) {

      sslContextId = niFiClientService.createSSLContext(niFiSinkDTO.getName() + " [SSL Context] ",
        keystoreFilename, keystorePassword, truststoreFilename, truststorePassword, path);

      CustomInfo customInfo = new CustomInfo();
      customInfo.setSslContextId(sslContextId);
      niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
      enableControllerServices(sslContextId);
    }

    String mqttConsumerId = niFiClientService
      .createMQTTConsumer(niFiSinkDTO.getName(), conf.getUri(), conf.getTopic(), conf.getQos(),
        conf.getQueueSize(), sslContextId, conf.getSchedulingPeriod(), path,
        isCommandHandler);

    if (isCommandHandler) {
      Set<String> relationship = new HashSet<>(
          List.of(SUCCESSFUL_RELATIONSHIP_TYPES.MESSAGE.getType()));

      String hardwareIdSetterId = niFiClientService.findProcessorIDByNameAndProcessGroup(
          ExistingProcessorSuffix.SET_HARDWARE_ID,
        path,
        NiFiSearchAlgorithm.NAME_ENDS_WITH);

      niFiClientService
        .connectComponentsInSameGroup(path, mqttConsumerId, hardwareIdSetterId, relationship);

      niFiClientService.changeProcessorGroupState(path, STATE.RUNNING);

      if (!niFiSinkDTO.isState()) {
        niFiClientService.changeProcessorStatus(niFiSinkDTO.getName(), path, STATE.STOPPED);
      }
    }
  }

  @Override
  public void updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    ConsumeMQTTConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());
    String sslContextId = null;
    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    if (!(Objects.equals(conf.getKeystoreFilename(), prevConf.getKeystoreFilename()) &&
      Objects.equals(conf.getKeystorePassword(), prevConf.getKeystorePassword()) &&
      Objects.equals(conf.getTruststoreFilename(), prevConf.getTruststoreFilename()) &&
      Objects.equals(conf.getTruststorePassword(), prevConf.getTruststorePassword()))) {

      CustomInfo customInfo = sinkDTO.getCustomInfo() != null ?
        objectMapper.readValue(sink.getCustomInfo(), CustomInfo.class) : null;

      if (customInfo == null) {
        sslContextId = niFiClientService
          .createSSLContextForExistingProcessor(sinkDTO.getName(), path,
            conf.getKeystoreFilename(), conf.getKeystorePassword(), conf.getTruststoreFilename(),
            conf.getTruststorePassword());

        customInfo = new CustomInfo();
        customInfo.setSslContextId(sslContextId);
        sinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
        enableControllerServices(sslContextId);
      } else {
        niFiClientService
          .updateSSLContext(customInfo.getSslContextId(), conf.getKeystoreFilename(),
            conf.getKeystorePassword(), conf.getTruststoreFilename(), conf.getTruststorePassword());
      }
    }

    niFiClientService
      .updateConsumerMQTT(processorId, sinkDTO.getName(), sslContextId, conf.getUri(),
        conf.getTopic(),
        conf.getQos(),
        conf.getQueueSize(), conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    if (niFiSinkDTO.getHandler() == NIFI_SINK_HANDLER.COMMAND.getType()) {
      niFiClientService.changeProcessorGroupState(path, STATE.STOPPED
      );
    }

    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    deleteControllerServices(niFiSinkDTO);
  }

  private void deleteControllerServices(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper.readValue(customInfoString, CustomInfo.class);
      niFiClientService.deleteController(customInfo.getSslContextId());
    }
  }

  @Override
  public void toggleSink(String name, String[] path, boolean isEnabled) throws IOException {
    niFiClientService.changeProcessorStatus(name, path,
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

  private ConsumeMQTTConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    return new Yaml(new Constructor(ConsumeMQTTConfiguration.class),
      representer)
      .load(configuration);
  }
}
